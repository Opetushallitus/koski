package fi.oph.koski.perftest

import java.io.ByteArrayOutputStream
import java.lang.System.currentTimeMillis
import java.util.zip.GZIPOutputStream

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging

import scala.annotation.tailrec

case class Operation(method: String = "GET",
  uri: String,
  queryParams: Iterable[(String, String)] = Seq.empty,
  headers: Iterable[(String, String)] = Seq.empty,
  body: Array[Byte] = null,
  gzip: Boolean = false,
  responseCodes: List[Int] = List(200)
)

abstract class KoskiPerfTester extends KoskidevHttpSpecification with Logging {
  lazy val roundCount = sys.env.getOrElse("PERFTEST_ROUNDS", "1000").toInt
  lazy val warmupRoundCount = sys.env.getOrElse("WARMUP_ROUNDS", "20").toInt
  lazy val serverCount = sys.env.getOrElse("KOSKI_SERVER_COUNT", "2").toInt
  lazy val threadCount = sys.env.getOrElse("PERFTEST_THREADS", "10").toInt

  def operation(i: Int): List[Operation]

  def executeTest = {
    logger.info("Using server " + baseUrl)
    logger.info(s"Rounds=$roundCount, Servers=$serverCount, Threads=$threadCount")
    val group = new ThreadGroup("perftest")

    def run(stats: StatsCollector, t: Int, rounds: Int): Unit = {
      var counter = 0
      def nextRound = synchronized {
        if (counter == rounds) {
          None
        } else {
          counter = counter + 1
          Some(counter - 1)
        }
      }

      val threads = 1 to t map { i =>
        val thread = new Thread(group, s"perftest-$i") {
          @tailrec
          override def run = {
            val started = currentTimeMillis
            nextRound match {
              case None =>
              case Some(round) =>
                val operations = operation(round)
                operations.foreach { o =>
                  val gzipHeaders = if (o.body != null && o.gzip) List(("Content-Encoding" -> "gzip")) else Nil
                  val cookieHeaders = Map("Cookie" -> s"SERVERID=koski-app${round % serverCount + 1}")
                  val contentTypeHeaders = if (o.body != null) jsonContent else Nil
                  val headers: Iterable[(String, String)] = o.headers ++ gzipHeaders ++ cookieHeaders ++ contentTypeHeaders ++ authHeaders()
                  val body = if (o.body != null && o.gzip) gzip(o.body) else o.body
                  val success = submit(o.method, o.uri, o.queryParams, headers, body) {
                    if (!o.responseCodes.contains(response.status)) {
                      logger.error(HttpStatusException(response.status, response.body, o.method, o.uri).getMessage)
                      false
                    } else {
                      true
                    }
                  }
                  stats.record(o.method + " " + o.uri, success, currentTimeMillis - started)
                }
                run
            }
          }
        }
        thread.start
        thread
      }

      threads.foreach{t => t.join}
      stats.log
    }
    if (warmupRoundCount > 0) {
      logger.info(s"Warming up for $warmupRoundCount rounds")
      run(new StatsCollector, threadCount, warmupRoundCount)
      logger.info("Reseting stats after warmup.")
    }
    val stats = new StatsCollector
    run(stats, threadCount, roundCount)

    group.destroy
    val failures: Int = stats.summary.failedCount
    if (failures > 0) {
      throw new RuntimeException(s"Test failed: $failures failures")
    }
  }

  private def gzip(bytes: Array[Byte]) = {
    val baos = new ByteArrayOutputStream
    val gzip = new GZIPOutputStream(baos)
    gzip.write(bytes)
    gzip.close()
    baos.toByteArray
  }
}

class StatsCollector extends Logging {
  private val startTimestamp: Long = currentTimeMillis

  case class Stats(timestamp: Long = currentTimeMillis(), successCount: Int = 0, failedCount: Int = 0, elapsedMsTotal: Long = 0) {
    def addSuccess(elapsedMs: Long) = this.copy(successCount = successCount + 1).addElapsed(elapsedMs)
    def addFailure(elapsedMs: Long) = this.copy(failedCount = failedCount + 1).addElapsed(elapsedMs)
    def addResult(success: Boolean, elapsedMs: Long) = if (success) addSuccess(elapsedMs) else addFailure(elapsedMs)
    def totalCount = { successCount + failedCount }
    def averageDuration = { elapsedMsTotal / totalCount }
    def requestsPerSec = { totalCount * 1000 / (timestamp - startTimestamp) }
    private def addElapsed(elapsed: Long) = this.copy(elapsedMsTotal = elapsedMsTotal + elapsed, timestamp = currentTimeMillis)
    def +(other: Stats) = Stats(
      Math.max(timestamp, other.timestamp),
      successCount + other.successCount,
      failedCount + other.failedCount,
      elapsedMsTotal + other.elapsedMsTotal
    )
  }
  private var statsByOperation: Map[String, Stats] = Map.empty

  private var previousLogged: Option[Long] = None

  private val logInterval = 1000

  def record(operation: String, success: Boolean, elapsedMs: Long): Unit = synchronized {
    val stats = statsByOperation.getOrElse(operation, new Stats()).addResult(success, elapsedMs)
    statsByOperation = statsByOperation + (operation -> stats)
    maybeLog
  }

  def getStatsByUri = synchronized { statsByOperation }

  def summary = {
    val values: Iterable[Stats] = getStatsByUri.values
    values.reduce[Stats](_ + _)
  }

  def maybeLog = synchronized {
    val now = currentTimeMillis
    val shouldLog = previousLogged match {
      case Some(timestamp) => now - timestamp > logInterval
      case None => true
    }
    if (shouldLog) {
      log
    }
  }
  def log = {
    getStatsByUri.foreach { case (operation, stats) =>
      logger.info(operation + " Success: " + stats.successCount + ", Failed: " + stats.failedCount + ", Average: " + stats.averageDuration + " ms, Req/sec: " + stats.requestsPerSec)
    }
    previousLogged = Some(currentTimeMillis)
  }
}

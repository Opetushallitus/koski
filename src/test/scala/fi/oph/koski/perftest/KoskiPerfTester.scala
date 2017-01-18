package fi.oph.koski.perftest

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging

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
  lazy val serverCount = sys.env.getOrElse("KOSKI_SERVER_COUNT", "2").toInt
  lazy val threadCount = sys.env.getOrElse("PERFTEST_THREADS", "10").toInt

  def operation(i: Int): List[Operation]

  private lazy val stats = new StatsCollector

  private var counter = 0
  private def nextRound = synchronized {
    counter = counter + 1
    counter - 1
  }

  private def shouldContinue = synchronized(counter < roundCount)

  def executeTest = {
    logger.info("Using server " + baseUrl)
    logger.info(s"Rounds=$roundCount, Servers=$serverCount, Threads=$threadCount")
    val group = new ThreadGroup("perftest")
    val threads = 1 to threadCount map { i =>
      val thread = new Thread(group, s"perftest-$i") {
        override def run = {
          while (shouldContinue) {
            val started = System.currentTimeMillis
            val round = nextRound
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
              stats.record(success, System.currentTimeMillis - started)
            }
          }
        }
      }
      thread.start
      thread
    }

    threads.foreach{t => t.join}
    group.destroy
    stats.log
    val failures: Int = stats.getStats.failedCount
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
  case class Stats(timestamp: Long = System.currentTimeMillis(), startTimestamp: Long = System.currentTimeMillis(), successCount: Int = 0, failedCount: Int = 0, elapsedMsTotal: Long = 0) {
    def addSuccess(elapsedMs: Long) = this.copy(successCount = successCount + 1).addElapsed(elapsedMs)
    def addFailure(elapsedMs: Long) = this.copy(failedCount = failedCount + 1).addElapsed(elapsedMs)
    def totalCount = { successCount + failedCount }
    def averageDuration = { elapsedMsTotal / totalCount }
    def requestsPerSec = { totalCount * 1000 / (timestamp - startTimestamp) }
    private def addElapsed(elapsed: Long) = this.copy(elapsedMsTotal = elapsedMsTotal + elapsed, timestamp = System.currentTimeMillis)
  }
  private var stats = Stats()
  var previousLogged: Stats = stats
  val logInterval = 1000
  def getStats = synchronized { stats }
  def record(success: Boolean, elapsedMs: Long): Unit = synchronized {
    stats = if (success) stats.addSuccess(elapsedMs) else stats.addFailure(elapsedMs)
    maybeLog
  }
  def maybeLog = synchronized {
    val now = System.currentTimeMillis
    if (now - previousLogged.timestamp > logInterval) {
      previousLogged = stats
      log
    }
  }
  def log = synchronized { logger.info("Success: " + stats.successCount + ", Failed: " + stats.failedCount + ", Average: " + stats.averageDuration + " ms, Req/sec: " + stats.requestsPerSec) }
}

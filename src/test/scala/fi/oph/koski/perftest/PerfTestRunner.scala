package fi.oph.koski.perftest

import java.io._
import java.lang.System.currentTimeMillis
import java.util.zip.GZIPOutputStream

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.log.{LoggerWithContext, Logging}
import fi.oph.koski.util.Streams

import scala.annotation.tailrec

object PerfTestRunner extends Logging {
  def executeTest(scenarios: PerfTestScenario*) = {
    val noop = () => {}

    val group = new ThreadGroup("perftest")
    scenarios.map { scenario =>
      if (scenario.warmupRoundCount > 0) {
        scenario.logger.info("Using server " + scenario.baseUrl)
        scenario.logger.info(s"Warming up for ${scenario.warmupRoundCount} rounds")
        val joiner = startScenario(scenario, group, new StatsCollector, scenario.threadCount, scenario.warmupRoundCount)
        joiner
      } else {
        noop
      }
    }.foreach(_())
    logger.info("**** Starting test ****")
    val stats = new StatsCollector
    scenarios.map(scenario => startScenario(scenario, group, stats, scenario.threadCount, scenario.roundCount)).foreach(_())
    logger.info("**** Finished test ****")
    val failures: Int = stats.summary.failedCount
    val successes: Int = stats.summary.successCount
    val successPercentage: Int = stats.summary.successPercentage
    val successThresholdPercentage: Int = scenarios.map(_.successThresholdPercentage).max
    if(successPercentage < successThresholdPercentage) {
      throw new RuntimeException(s"Test failed: $successPercentage% succeeded, while success threshold was $successThresholdPercentage%. $failures failures and $successes successes in total.")
    } else {
      logger.info(s"Test passed: $successPercentage% succeeded, while success threshold was $successThresholdPercentage%. $failures failures and $successes successes in total.")
    }
  }

  private def startScenario(scenario: PerfTestScenario, group: ThreadGroup, stats: StatsCollector, t: Int, rounds: Int): (() => Unit) = {
    scenario.logger.info(s"Rounds=${scenario.roundCount}, Servers=${scenario.serverCount}, Threads=${scenario.threadCount}")
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
              val operations = scenario.operation(round)
              operations.foreach { o =>
                val gzipHeaders = if (o.body != null && o.gzip) List(("Content-Encoding" -> "gzip")) else Nil
                val cookieHeaders = Map("Cookie" -> s"SERVERID=koski-app${round % scenario.serverCount + 1}")
                val contentTypeHeaders = if (o.body != null) scenario.jsonContent else Nil
                val headers: Iterable[(String, String)] = o.headers ++ gzipHeaders ++ cookieHeaders ++ contentTypeHeaders ++ scenario.authHeaders()
                val body = if (o.body != null && o.gzip) gzip(o.body) else o.body
                val success = try {
                  scenario.submit(o.method, o.uri, o.queryParams, headers, body) {
                    if (!o.responseCodes.contains(scenario.response.status)) {
                      scenario.logger.error(HttpStatusException(scenario.response.status, scenario.response.body, o.method, o.uri).getMessage)
                      false
                    } else {
                      if (scenario.readBody) {
                        toDevNull(scenario.response.inputStream)
                      }
                      scenario.bodyValidator
                    }
                  }
                } catch {
                  case e: Exception =>
                    scenario.logger.error(e.getMessage)
                    false
                }
                val elapsed = currentTimeMillis - started
                if (elapsed <= scenario.maximumExpectedDurationMs) {
                  stats.record(o.method + " " + o.uriPattern.getOrElse(o.uri), success, elapsed)(scenario.logger)
                } else {
                  scenario.logger.warn(s"Round took $elapsed milliseconds, while maximum expected duration was ${scenario.maximumExpectedDurationMs} milliseconds")
                  stats.record(o.method + " " + o.uriPattern.getOrElse(o.uri), false, elapsed)(scenario.logger)
                }
              }
              run
          }
        }
      }
      thread.start
      thread
    }

    () => {
      threads.foreach{t => t.join}
      scenario.logger.info("Finished")
      stats.log(scenario.logger)
    }
  }

  private def toDevNull(stream: InputStream) =
    Streams.pipeTo(new BufferedInputStream(stream), (b: Int) => {})

  private def gzip(bytes: Array[Byte]) = {
    val baos = new ByteArrayOutputStream
    val gzip = new GZIPOutputStream(baos)
    gzip.write(bytes)
    gzip.close()
    baos.toByteArray
  }
}

case class Stats(
  startTimestamp: Long,
  timestamp: Long = currentTimeMillis(),
  successCount: Int = 0,
  failedCount: Int = 0,
  elapsedMsTotal: Long = 0,
  successPercentage: Int = 0
) {
  def addSuccess(elapsedMs: Long) = this.copy(successCount = successCount + 1).addElapsed(elapsedMs).addSuccessPercentage()
  def addFailure(elapsedMs: Long) = this.copy(failedCount = failedCount + 1).addElapsed(elapsedMs).addSuccessPercentage()
  def addResult(success: Boolean, elapsedMs: Long) = if (success) addSuccess(elapsedMs) else addFailure(elapsedMs)
  def totalCount = { successCount + failedCount }
  def averageDuration = { elapsedMsTotal / totalCount }
  def requestsPerSec = { totalCount * 1000 / (timestamp - startTimestamp) }
  private def addElapsed(elapsed: Long) = this.copy(elapsedMsTotal = elapsedMsTotal + elapsed, timestamp = currentTimeMillis)
  private def addSuccessPercentage(): Stats = if(totalCount > 0) {
    this.copy(successPercentage = math.round(successCount.toDouble / totalCount.toDouble * 100).toInt)
  } else { this.copy(successPercentage = 0) }
  def +(other: Stats) = Stats(
    startTimestamp,
    Math.max(timestamp, other.timestamp),
    successCount + other.successCount,
    failedCount + other.failedCount,
    elapsedMsTotal + other.elapsedMsTotal
  ).addSuccessPercentage()
}

class StatsCollector {
  private val startTimestamp: Long = currentTimeMillis

  private var statsByOperation: Map[String, Stats] = Map.empty

  private var previousLogged: Option[Long] = None

  private val logInterval = 1000

  def record(operation: String, success: Boolean, elapsedMs: Long)(implicit logger: LoggerWithContext): Unit = synchronized {
    val stats = statsByOperation.getOrElse(operation, new Stats(startTimestamp)).addResult(success, elapsedMs)
    statsByOperation = statsByOperation + (operation -> stats)
    maybeLog
  }

  def getStatsByOperation = synchronized { statsByOperation }

  def summary = {
    val values: Iterable[Stats] = getStatsByOperation.values
    values.reduce[Stats](_ + _)
  }

  def maybeLog(implicit logger: LoggerWithContext) = synchronized {
    val now = currentTimeMillis
    val shouldLog = previousLogged match {
      case Some(timestamp) => now - timestamp > logInterval
      case None => true
    }
    if (shouldLog) {
      log
    }
  }
  def log(implicit logger: LoggerWithContext) = {
    getStatsByOperation.foreach { case (operation, stats) =>
      logger.info(operation + " Success: " + stats.successCount + ", Failed: " + stats.failedCount + ", Average: " + stats.averageDuration + " ms, Req/sec: " + stats.requestsPerSec)
    }
    previousLogged = Some(currentTimeMillis)
  }
}

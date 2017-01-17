package fi.oph.koski.perftest

import java.io.ByteArrayOutputStream
import java.util.concurrent.{ExecutorService, Executors}
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

case class PerfTestExecutor(threadCount: Int = 10, operation: Int => List[Operation]) extends KoskidevHttpSpecification with Logging with App {
  logger.info("Using server " + baseUrl)

  private lazy val pool: ExecutorService = Executors.newFixedThreadPool(threadCount)
  private lazy val stats = new StatsCollector
  private var counter = 0
  private def nextId = synchronized {
    counter = counter + 1
    counter - 1
  }

  def executeTest = {
    1 to threadCount foreach { _ =>
      pool.execute(() => {
        while(true) {
          val started = System.currentTimeMillis
          val i = nextId
          val success = try {
            val operations = operation(i)
            operations.foreach { o =>
              val gzipHeaders = if (o.body != null && o.gzip) List(("Content-Encoding" -> "gzip")) else Nil
              val headers: Iterable[(String, String)] = o.headers ++ gzipHeaders
              val body = if (o.body != null && o.gzip) gzip(o.body) else o.body
              submit(o.method, o.uri, o.queryParams, headers, body) {
                if (!o.responseCodes.contains(response.status)) {
                  throw HttpStatusException(response.status, response.body, o.method, o.uri)
                }
                response.status should equal(200)
              }
            }
            true
          } catch {
            case e: Exception =>
              logger.error(e)(s"Error")
              false
          }
          stats.record(success, System.currentTimeMillis - started)
        }
      })
    }
  }

  private def gzip(bytes: Array[Byte]) = {
    val baos = new ByteArrayOutputStream
    val gzip = new GZIPOutputStream(baos)
    gzip.write(bytes)
    gzip.close()
    baos.toByteArray
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
    var stats = Stats()
    var previousLogged: Stats = stats
    val logInterval = 1000
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
    def log = { logger.info("Success: " + stats.successCount + ", Failed: " + stats.failedCount + ", Average: " + stats.averageDuration + " ms, Req/sec: " + stats.requestsPerSec) }
  }
}

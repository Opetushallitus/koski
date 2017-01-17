package fi.oph.koski.perftest

import java.util.concurrent.{ExecutorService, Executors}

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.OidHenkilö
import org.scalatest.Matchers

import scala.util.Random

object RandomOpiskeluoikeusGetter extends KoskidevHttpSpecification with Logging with App {
  PerfTestExecutor(
    threadCount = 10,
    operation = { x => Operation(uri = s"api/oppija/${RandomOppijaOid.nextOid}", headers = (authHeaders() ++ jsonContent ++ Map("Cookie" -> s"SERVERID=koski-app${x % 2 + 1}")))}
  ).executeTest
}

case class PerfTestExecutor(threadCount: Int = 10, operation: Int => Operation) extends KoskidevHttpSpecification with Logging with App {
  private lazy val pool: ExecutorService = Executors.newFixedThreadPool(threadCount)
  private lazy val stats = new Stats
  private var counter = 0
  private def nextId = synchronized {
    counter = counter + 1
    counter - 1
  }

  def executeTest = {
    1 to threadCount foreach { _ =>
      pool.execute(() => {
        while(true) {
          val henkilöOid = RandomOppijaOid.nextOid
          val started = System.currentTimeMillis
          val i = nextId
          val success = try {
            val o = operation(i)
            submit(o.method, o.uri, o.queryParams, o.headers, o.body) {
              response.status should equal(200)
              true
            }
          } catch {
            case e: Exception =>
              logger.error(e)(s"Error while fetching oppija $henkilöOid")
              false
          }
          stats.record(success, System.currentTimeMillis - started)
        }
      })
    }
  }

  class Stats extends Logging {
    var successCount = 0
    var elapsedMsTotal: Long = 0
    var failedCount = 0
    var previousLog: Long = 0
    val logInterval = 1000
    def record(success: Boolean, elapsedMs: Long): Unit = synchronized {
      if (success) successCount = successCount + 1 else failedCount = failedCount + 1
      elapsedMsTotal = elapsedMsTotal + elapsedMs
      maybeLog
    }
    def maybeLog = {
      val now = System.currentTimeMillis
      if (now - previousLog > logInterval) {
        previousLog = now
        log
      }
    }
    def log = synchronized { logger.info("Success: " + successCount + " Failed: " + failedCount + " Average: " + averageDuration + " ms") }
    def totalCount = synchronized { successCount + failedCount }
    def averageDuration = synchronized { elapsedMsTotal / totalCount }
  }
}

case class Operation(method: String = "GET",
  uri: String,
  queryParams: Iterable[(String, String)] = Seq.empty,
  headers: Iterable[(String, String)] = Seq.empty,
  body: Array[Byte] = null)

object RandomOppijaOid extends KoskidevHttpSpecification with Matchers with Logging {
  val url: String = s"api/oppija/oids?pageNumber=0&pageSize=500000"
  lazy val oids = get(url, headers = (authHeaders() ++ jsonContent)) {
    response.status should equal(200)
    Json.read[List[String]](body)
  }

  lazy val oidIterator = {
    logger.info("Looping through " + oids.length + " oids")
    Iterator.continually(Random.shuffle(oids).iterator).flatten
  }

  def nextOid = oidIterator.next
}


case class FlatOppija(henkilö: OidHenkilö)
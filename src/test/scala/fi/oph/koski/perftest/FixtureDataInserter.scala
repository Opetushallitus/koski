package fi.oph.koski.perftest

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import fi.oph.koski.http.DefaultHttpTester
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija, UusiHenkilö}
import org.scalatra.test.ClientResponse

abstract class FixtureDataInserter extends App with DefaultHttpTester with Logging {
  val pool: ExecutorService = Executors.newFixedThreadPool(10)
  val amount = 1000000

  val t0 = System.currentTimeMillis()

  1 to amount foreach { x =>
    pool.execute(new Runnable {
      override def run(): Unit = {
        val oikeudet = opiskeluoikeudet(x)
        val nimi = "Koski-Perf-" + x
        val henkilö: UusiHenkilö = Henkilö(RandomHetu.nextHetu, nimi, nimi, nimi)
        oikeudet.zipWithIndex.foreach { case(oikeus, index) =>
          val oppija: Oppija = Oppija(henkilö, List(oikeus))
          val body = Json.write(oppija).getBytes("utf-8")
          println(s"Insert $nimi (${index + 1} / ${oikeudet.length})")
          put("api/oppija", body = body, headers = (authHeaders() ++ jsonContent)) {
            logger.info(nimi + " " + response.status)
            handleResponse(response, oikeus)
          }
        }
      }
    })
  }

  pool.shutdown()
  pool.awaitTermination(48, TimeUnit.HOURS)
  private val elapsed: Long = System.currentTimeMillis() - t0
  logger.info("Luotu " + amount + " opiskeluoikeutta ajassa " + elapsed + "ms")
  logger.info("Ops/sec " + (amount * 1000 / elapsed))

  def opiskeluoikeudet(oppijaIndex: Int): List[Opiskeluoikeus]

  def handleResponse(response: ClientResponse, oikeus: Opiskeluoikeus) = {
    if(response.status != 200) {
      logger.info(oikeus.id + " failed")
      logger.info(response.body)
    }
  }

}



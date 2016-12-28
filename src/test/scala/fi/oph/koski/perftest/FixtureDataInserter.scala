package fi.oph.koski.perftest

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.perftest.RandomName._
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija, UusiHenkilö}
import org.scalatra.test.ClientResponse

abstract class FixtureDataInserter extends App with KoskidevHttpSpecification with Logging {
  val pool: ExecutorService = Executors.newFixedThreadPool(20)
  val amount = 1000000

  val t0 = System.currentTimeMillis()

  val created: AtomicInteger = new AtomicInteger(0)

  println("Using server " + baseUrl)

  1 to amount foreach { x =>
    pool.execute(new Runnable {
      override def run(): Unit = {
        val oikeudet = opiskeluoikeudet(x)
        val kutsumanimi = randomFirstName
        val henkilö: UusiHenkilö = Henkilö(RandomHetu.nextHetu, kutsumanimi + " " + randomFirstName, kutsumanimi, randomLastName)
        oikeudet.zipWithIndex.foreach { case(oikeus, index) =>
          val oppija: Oppija = Oppija(henkilö, List(oikeus))
          val body = Json.write(oppija).getBytes("utf-8")
          put("api/oppija", body = body, headers = (authHeaders() ++ jsonContent ++ Map("Cookie" -> s"SERVERID=koski-app${x % 2 + 1}"))) {
            if (x % (Math.max(1, amount / 10000)) == 1) logger.info(henkilö.kokonimi + " " + response.status)
            handleResponse(response, oikeus, henkilö)
          }
        }
        val currentlyCreated = created.addAndGet(1)
        if(currentlyCreated % 100 == 0) {
          logger.info("Luotu: " + currentlyCreated + " oppijaa ajassa: " + (System.currentTimeMillis() - t0) / 1000 + "s")
        }
      }
    })
  }

  pool.shutdown()
  pool.awaitTermination(48, TimeUnit.HOURS)
  private val elapsed: Long = System.currentTimeMillis() - t0
  logger.info("Luotu " + amount + " oppijaa ajassa " + elapsed + "ms")
  logger.info("Oppijaa/sekunti " + (amount * 1000 / elapsed))

  def opiskeluoikeudet(oppijaIndex: Int): List[Opiskeluoikeus]

  def handleResponse(response: ClientResponse, oikeus: Opiskeluoikeus, henkilö: UusiHenkilö) = {

    if(response.status != 200) {
      logger.info(henkilö.kokonimi + " failed")
      logger.info(response.body)
    }
  }

}



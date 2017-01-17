package fi.oph.koski.perftest

import java.util.concurrent.{ExecutorService, Executors}

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.perftest.RandomName._
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija, UusiHenkilö}

abstract class FixtureDataInserter extends App with KoskidevHttpSpecification with Logging {
  val amount = 1000000
  val serverCount = 2
  val responseCodes = List(200)

  PerfTestExecutor(threadCount = 20,
    operation = {x =>
      val oikeudet = opiskeluoikeudet(x)
      val kutsumanimi = randomFirstName
      val henkilö: UusiHenkilö = Henkilö(RandomHetu.nextHetu, kutsumanimi + " " + randomFirstName, kutsumanimi, randomLastName)
      oikeudet.zipWithIndex.map { case(oikeus, index) =>
        val oppija: Oppija = Oppija(henkilö, List(oikeus))
        val body = Json.write(oppija).getBytes("utf-8")
        Operation(
          "PUT", "api/oppija",
          body = body,
          headers = (authHeaders() ++ jsonContent ++ Map("Cookie" -> s"SERVERID=koski-app${x % serverCount + 1}")),
          responseCodes = responseCodes)
      }
    }
  ).executeTest

  def opiskeluoikeudet(oppijaIndex: Int): List[Opiskeluoikeus]
}



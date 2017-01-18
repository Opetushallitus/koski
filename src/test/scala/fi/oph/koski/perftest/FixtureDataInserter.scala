package fi.oph.koski.perftest

import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.perftest.RandomName._
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija, UusiHenkilö}

abstract class FixtureDataInserter extends KoskiPerfTester with App with Logging {
  val responseCodes = List(200)
  val hetu = new RandomHetu()

  def opiskeluoikeudet(oppijaIndex: Int): List[Opiskeluoikeus]

  def operation(x: Int) = {
    val oikeudet = opiskeluoikeudet(x)
    val kutsumanimi = randomFirstName
    val henkilö: UusiHenkilö = Henkilö(hetu.nextHetu, kutsumanimi + " " + randomFirstName, kutsumanimi, randomLastName)
    oikeudet.zipWithIndex.map { case(oikeus, index) =>
      val oppija: Oppija = Oppija(henkilö, List(oikeus))
      val body = Json.write(oppija).getBytes("utf-8")
      Operation(
        "PUT", "api/oppija",
        body = body,
        gzip = true,
        responseCodes = responseCodes)
    }
  }

  executeTest
}

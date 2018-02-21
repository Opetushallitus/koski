package fi.oph.koski.perftest

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perftest.RandomName._
import fi.oph.koski.schema._

abstract class FixtureDataInserterScenario extends PerfTestScenario {
  val responseCodes = List(200)
  val hetu = new RandomHetu()

  def opiskeluoikeudet(oppijaIndex: Int): List[Opiskeluoikeus]

  def operation(x: Int) = {
    val oikeudet = opiskeluoikeudet(x)
    val kutsumanimi = randomFirstName
    val henkilö: UusiHenkilö = UusiHenkilö(hetu.nextHetu, kutsumanimi + " " + randomFirstName, Some(kutsumanimi), randomLastName)
    oikeudet.map { oikeus =>
      val oppija: Oppija = Oppija(henkilö, List(oikeus))
      val body = JsonSerializer.writeWithRoot(oppija).getBytes("utf-8")
      Operation(
        "PUT", "api/oppija",
        body = body,
        gzip = true,
        responseCodes = responseCodes)
    }
  }
}

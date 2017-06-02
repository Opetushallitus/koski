package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.MockAuthenticationServiceClient
import fi.oph.koski.henkilo.MockOppijat.eero
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class KoskiScheduledTasksSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  lazy val application = KoskiApplicationForTests
  "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
    authServiceClient.modify(TäydellisetHenkilötiedot(eero.oid, eero.etunimet, eero.kutsumanimi, "Uusisukunimi"))
    new UpdateHenkilot(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis}}""")))
    application.elasticSearch.refreshIndex
    val päivitettytPerustiedot = application.perustiedotRepository.findHenkilöPerustiedot(eero.oid).get
    päivitettytPerustiedot.sukunimi should equal("Uusisukunimi")
  }

  override def afterEach(): Unit = authServiceClient.reset()
  private def authServiceClient = application.authenticationServiceClient.asInstanceOf[MockAuthenticationServiceClient]
}

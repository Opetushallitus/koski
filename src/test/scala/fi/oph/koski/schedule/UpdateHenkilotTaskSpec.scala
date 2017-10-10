package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.MockOppijat.{eero, eerola}
import fi.oph.koski.henkilo.authenticationservice.MockAuthenticationServiceClient
import fi.oph.koski.schema.{TäydellisetHenkilötiedot, TäydellisetHenkilötiedotWithMasterInfo}
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class UpdateHenkilotTaskSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  lazy val application = KoskiApplicationForTests
  "Nimitietojen päivittyminen" - {
    "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
      modify(TäydellisetHenkilötiedotWithMasterInfo(TäydellisetHenkilötiedot(eero.oid, eero.etunimet, eero.kutsumanimi, "Uusisukunimi"), None))
      val päivitetytPerustiedot = application.perustiedotRepository.findHenkilöPerustiedot(eero.oid).get
      päivitetytPerustiedot.sukunimi should equal("Uusisukunimi")
    }
  }
  "Oppijoiden linkitys" - {
    "Aluksi ei linkitety" in {
      application.perustiedotRepository.findMasterHenkilöPerustiedot(eero.oid) should equal(None)
    }

    "Lisää linkityksen perustietoihin" in {
      modify(eero.copy(master = Some(eerola.henkilö)))
      application.perustiedotRepository.findMasterHenkilöPerustiedot(eero.oid)
        .map(tiedot => (tiedot.oid, tiedot.sukunimi)) should equal(Some((eerola.oid, eerola.sukunimi)))
    }

    "Poistaa linkityksen perustiedoista" in {
      modify(eero.copy(
        master = None,
        henkilö = eero.henkilö.copy(etunimet = "asdf"))  // <- muutetaan myös etunimeä koska MockAuthenticationServiceClient.findChangedOppijaOids ei muuten huomaa muutosta
      )
      application.perustiedotRepository.findMasterHenkilöPerustiedot(eero.oid) should equal(None)
    }
  }

  private def modify(tiedot: TäydellisetHenkilötiedotWithMasterInfo): Unit = {
    authServiceClient.modify(tiedot)
    new UpdateHenkilotTask(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis}}""")))
    application.elasticSearch.refreshIndex
  }

  override def afterEach(): Unit = authServiceClient.reset()
  private def authServiceClient = application.authenticationServiceClient.asInstanceOf[MockAuthenticationServiceClient]
}

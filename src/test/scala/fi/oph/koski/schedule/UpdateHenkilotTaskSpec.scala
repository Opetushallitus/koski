package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.util.Futures
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class UpdateHenkilotTaskSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  lazy val application = KoskiApplicationForTests
  "Nimitietojen päivittyminen" - {
    Futures.await(application.perustiedotIndexer.init)
    "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
      modify(OppijaHenkilöWithMasterInfo(eero.copy(sukunimi = "Uusisukunimi"), None))
      val päivitetytPerustiedot = application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).get
      päivitetytPerustiedot.sukunimi should equal("Uusisukunimi")
    }
  }
  "Oppijoiden linkitys" - {
    "Aluksi ei linkitety" in {
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).map(_.oid) should equal(Some(eero.oid))
    }

    "Lisää linkityksen perustietoihin" in {
      modify(OppijaHenkilöWithMasterInfo(henkilö = eero, master = Some(eerola)))
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid)
        .map(tiedot => (tiedot.oid, tiedot.sukunimi)) should equal(Some((eerola.oid, eerola.sukunimi)))
    }

    "Poistaa linkityksen perustiedoista" in {
      modify(OppijaHenkilöWithMasterInfo(
        master = None,
        henkilö = eero.copy(etunimet = "asdf"))  // <- muutetaan myös etunimeä koska MockAuthenticationServiceClient.findChangedOppijaOids ei muuten huomaa muutosta
      )
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).map(_.oid) should equal(Some(eero.oid))
    }

    "Lisää master tiedot jos ei löydy Koskesta" in {
      modify(OppijaHenkilöWithMasterInfo(henkilö = eero, master = Some(masterEiKoskessa)))
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid)
        .map(tiedot => (tiedot.oid, tiedot.sukunimi)) should equal(Some((masterEiKoskessa.oid, masterEiKoskessa.sukunimi)))
    }
  }

  private def modify(tiedot: OppijaHenkilöWithMasterInfo): Unit = {
    henkilöFacade.modifyMock(tiedot)
    new UpdateHenkilotTask(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis}}""")))
    application.perustiedotIndexer.refreshIndex
  }

  override def afterEach(): Unit = henkilöFacade.resetMock()
  private def henkilöFacade = application.opintopolkuHenkilöFacade.asInstanceOf[MockOpintopolkuHenkilöFacade]
}

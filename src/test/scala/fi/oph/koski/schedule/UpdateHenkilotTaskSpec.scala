package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.ExamplesLukio.lukioKesken
import fi.oph.koski.henkilo.MockOppijat.{eero, eerola, masterEiKoskessa, slaveMasterEiKoskessa}
import fi.oph.koski.henkilo.{MockOpintopolkuHenkilöFacade, VerifiedHenkilöOid}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.makePerustiedot
import fi.oph.koski.schema.{TäydellisetHenkilötiedot, TäydellisetHenkilötiedotWithMasterInfo}
import fi.oph.koski.util.Futures
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class UpdateHenkilotTaskSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  lazy val application = KoskiApplicationForTests
  "Nimitietojen päivittyminen" - {
    Futures.await(application.perustiedotIndexer.init)
    "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
      modify(TäydellisetHenkilötiedotWithMasterInfo(TäydellisetHenkilötiedot(eero.oid, eero.etunimet, eero.kutsumanimi, "Uusisukunimi"), None))
      val päivitetytPerustiedot = application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).get
      päivitetytPerustiedot.sukunimi should equal("Uusisukunimi")
    }
  }
  "Oppijoiden linkitys" - {
    "Aluksi ei linkitety" in {
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).map(_.oid) should equal(Some(eero.oid))
    }

    "Lisää linkityksen perustietoihin" in {
      modify(eero.copy(master = Some(eerola.henkilö)))
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid)
        .map(tiedot => (tiedot.oid, tiedot.sukunimi)) should equal(Some((eerola.oid, eerola.sukunimi)))
    }

    "Poistaa linkityksen perustiedoista" in {
      modify(eero.copy(
        master = None,
        henkilö = eero.henkilö.copy(etunimet = "asdf"))  // <- muutetaan myös etunimeä koska MockAuthenticationServiceClient.findChangedOppijaOids ei muuten huomaa muutosta
      )
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(eero.oid).map(_.oid) should equal(Some(eero.oid))
    }

    "Lisää master tiedot jos ei löydy Koskesta" in {
      addToFixture(slaveMasterEiKoskessa.copy(master = None))
      modify(slaveMasterEiKoskessa.copy(henkilö = slaveMasterEiKoskessa.henkilö.copy(etunimet = "Foo")))
      application.perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(slaveMasterEiKoskessa.henkilö.oid).map(_.sukunimi) should equal(Some(masterEiKoskessa.henkilö.sukunimi))
    }
  }

  private def addToFixture(oppija: TäydellisetHenkilötiedotWithMasterInfo) = {
    implicit val user = KoskiSession.systemUser
    application.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(oppija.henkilö), lukioKesken, allowUpdate = true).map(_.id).foreach { id =>
      application.perustiedotIndexer.updateBulk(List(makePerustiedot(id, lukioKesken, Some(oppija))), upsert = true)
    }
  }

  private def modify(tiedot: TäydellisetHenkilötiedotWithMasterInfo): Unit = {
    henkilöFacade.modify(tiedot)
    new UpdateHenkilotTask(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis}}""")))
    application.elasticSearch.refreshIndex
  }

  override def afterEach(): Unit = henkilöFacade.reset()
  private def henkilöFacade = application.opintopolkuHenkilöFacade.asInstanceOf[MockOpintopolkuHenkilöFacade]
}

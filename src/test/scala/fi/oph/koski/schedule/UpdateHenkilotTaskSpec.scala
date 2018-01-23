package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.HetuGenerator.generateHetu
import fi.oph.koski.henkilo.MockOpintopolkuHenkilöFacade
import fi.oph.koski.henkilo.MockOppijat.{eero, eerola}
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.UusiHenkilö
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.{TäydellisetHenkilötiedot, TäydellisetHenkilötiedotWithMasterInfo}
import fi.oph.koski.util.Futures
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class UpdateHenkilotTaskSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  lazy val application = KoskiApplicationForTests
  "Nimitietojen päivittyminen" - {
    Futures.await(application.perustiedotIndexer.init)
    "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
      1 to 1100 foreach { x =>
        henkilöFacade.findOrCreate(UusiHenkilö(Some(generateHetu), "Changed", x.toString, x.toString, "", None))
      }
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
  }

  private def modify(tiedot: TäydellisetHenkilötiedotWithMasterInfo): Unit = {
    henkilöFacade.modify(tiedot)
    runHenkilötiedotUpdateTwice
    application.elasticSearch.refreshIndex
  }

  private def runHenkilötiedotUpdateTwice = {
    val ctx = new UpdateHenkilotTask(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis - 120 * 60 * 1000}}""")))
    val newCtx = JsonSerializer.extract[HenkilöUpdateContext](ctx.get)
    new UpdateHenkilotTask(application).updateHenkilöt(Some(parseJson(s"""{"lastRun": ${newCtx.lastRun}}""")))
  }

  override def afterEach(): Unit = henkilöFacade.reset()
  private def henkilöFacade = application.opintopolkuHenkilöFacade.asInstanceOf[MockOpintopolkuHenkilöFacade]
}

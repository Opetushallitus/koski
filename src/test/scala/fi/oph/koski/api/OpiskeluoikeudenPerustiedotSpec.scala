package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, eerola, lukiolainen}
import fi.oph.koski.schema.LocalizedString.{english, finnish, swedish}
import fi.oph.koski.schema._
import fi.oph.koski.util.Wait
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class OpiskeluoikeudenPerustiedotSpec extends FreeSpec with BeforeAndAfterAll with LocalJettyHttpSpecification with SearchTestMethods with MuuAmmatillinenTestMethods[MuunAmmatillisenKoulutuksenSuoritus] {
  override protected def beforeAll(): Unit = createEnglanninkielinenSuoritus

  "Perustiedot" - {
    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain englanninkielisellä nimellä" in {
      searchPerustiedot("respect", "fi") should equal(List("Respect: Helping us sustain a harassment free workplace"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain englanninkielisellä nimellä" in {
      searchPerustiedot("respect", "sv") should equal(List("Respect: Helping us sustain a harassment free workplace"))
    }

    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain ruotsinkielisellä nimellä" in {
      searchPerustiedot("respekt", "fi") should equal(List("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain ruotsinkielisellä nimellä" in {
      searchPerustiedot("respekt", "sv") should equal(List("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats"))
    }

    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain suomenkielisellä nimellä" in {
      searchPerustiedot("kunnioitus", "fi") should equal(List("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain suomenkielisellä nimellä" in {
      searchPerustiedot("kunnioitus", "sv") should equal(List("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa"))
    }
  }

  private def searchPerustiedot(tutkintoHakuString: String, lang: String): List[String] = {
    Wait.until(searchForPerustiedot(Map("tutkintohaku" -> tutkintoHakuString)).nonEmpty, timeoutMs = 1000)
    searchForPerustiedot(Map("tutkintohaku" -> tutkintoHakuString)).flatMap(_.suoritukset.flatMap(_.koulutusmoduuli.tunniste.nimi)).map(_.get(lang))
  }

  private def createEnglanninkielinenSuoritus = {
    putAmmatillinenPäätasonSuoritus(suoritus(english("Respect: Helping us sustain a harassment free workplace")))(verifyResponseStatusOk())
    putAmmatillinenPäätasonSuoritus(suoritus(swedish("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats")), henkilö = asUusiOppija(eerola))(verifyResponseStatusOk())
    putAmmatillinenPäätasonSuoritus(suoritus(finnish("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa")), henkilö = asUusiOppija(lukiolainen))(verifyResponseStatusOk())
    KoskiApplicationForTests.perustiedotSyncScheduler.sync
    KoskiApplicationForTests.perustiedotIndexer.refreshIndex
  }

  def suoritus(tutkinnonTunniste: LocalizedString): MuunAmmatillisenKoulutuksenSuoritus = muunAmmatillisenKoulutuksenSuoritus(
    koulutusmoduuli = PaikallinenMuuAmmatillinenKoulutus(
      tunniste = PaikallinenKoodi(koodiarvo = "RESPECT", tutkinnonTunniste),
      laajuus = None,
      kuvaus = finnish("xyz")
    ),
    toimipiste = stadinToimipiste
  )

  override protected def defaultPäätasonSuoritus: MuunAmmatillisenKoulutuksenSuoritus = suoritus(LocalizedString.empty)
}

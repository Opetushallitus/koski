package fi.oph.koski.raportit

import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmattitutkintoExample, ExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.raportointikanta.{ROsasuoritusRow, RaportointikantaTestMethods}
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class AmmatillinenTutkintoRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with OpiskeluoikeusTestMethodsAmmatillinen
    with BeforeAndAfterAll
    with DirtiesFixtures {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  override protected def alterFixture(): Unit = {
    putOppija(Oppija(KoskiSpecificMockOppijat.ammattilainen, List(AmmatillinenExampleData.opiskeluoikeus().copy(
      suoritukset = List(AmmatillinenExampleData.ammatillisenTutkinnonSuoritusKorotetuillaOsasuorituksilla()),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
    )))) {
      verifyResponseStatusOk()
      reloadRaportointikanta
    }
  }

  lazy val repository = AmmatillisenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Suoritustietojen tarkistusraportti" - {
    lazy val rivi = {
      reloadRaportointikanta
      val rows = testiHenkilöRaporttiRows(defaultRequest)
      rows.length should equal(2)
      rows.head
    }

    "Sisältää oikeat tiedot" in {
      rivi.opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
      rivi.tutkinto should equal("361902")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.tutkintonimike should equal("Ympäristönhoitaja")
      rivi.päätasonSuorituksenNimi should equal("Luonto- ja ympäristöalan perustutkinto")
      rivi.päätasonSuorituksenSuoritusTapa should equal("Ammatillinen perustutkinto")
      rivi.päätasonSuorituksenTila should equal("Valmis")
      rivi.viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
      rivi.viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("lasna")
      rivi.opintojenRahoitukset should equal("4")
      rivi.ostettu should equal(false)
      rivi.yksiloity should equal(true)
    }

    "Laskenta" - {
      "Suorituksia yhteesä" in {
        rivi.suoritettujenOpintojenYhteislaajuus should equal("180.0")
      }
      "Ammatilliset tutkinnon osat" - {
        "Valmiiden ammatillisten tutkinnon osien lukumäärä" in {
          rivi.valmiitAmmatillisetTutkinnonOsatLkm should equal("6")
        }
        "Pakolliset ammatilliset tutkinnon osat" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatLkm should equal("6")
        }
        "Valinnaiset ammatilliset tutkinnon osat" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatLkm should equal("0")
        }
        "Näyttöjä ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("3")
        }
        "Tunnustettuja ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("0")
        }
        "Rahoituksen piirissä tunnustettuja ammatillisia tutkinnon osia" in {
          rivi.rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal("0")
        }
        "Suoritetut ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("135.0")
        }
        "Tunnustetut ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        }
        "Pakolliset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("135.0")
        }
        "Valinnaiset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        }
      }
      "Yhteiset tutkinnon osat" - {
        "Valmiit yhteiset tutkinnon osat lukumäärä" in {
          rivi.valmiitYhteistenTutkinnonOsatLkm should equal("4")
        }
        "Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal("8")
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal("1")
        }
        "Tunnustettuja yhteisten tutkinnon osan osa-alueita valmiista yhteisen tutkinnon osa-alueista" in {
          rivi.tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal("1")
        }
        "Rahoituksen piirissä tutkinnon osan osa-alueita valmiissa yhteisten tutkinnon osan osa-aluiesta" in {
          rivi.rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal("0")
        }
        "Tunnustettuja tutkinnon osia valmiista yhteisen tutkinnon osista" in {
          rivi.tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal("0")
        }
        "Rahoituksen piirissä tunnustetuista yhteisistä tutkinnon osista" in {
          rivi.rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal("0")
        }
        "Suoritettuja yhteisten tutkinnon osien yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienYhteislaajuus should equal("35.0")
        }
        "Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("35.0")
        }
        "Tunnustettujen yhteisten tutkinnon osien osuus valmiista yhteisistä tutkinnon osista" in {
          rivi.tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus should equal("0.0")
        }
        "Pakollisten yhteisten tutkinnon osioen osa-alueiden yhteislaajuus" in {
          rivi.pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("32.0")
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal("3.0")
        }
      }
      "Valmiit vapaavalintaiset tutkinnon osat lukumäärä" in {
        rivi.valmiitVapaaValintaisetTutkinnonOsatLkm should equal("1")
      }
      "Valmiit tutkintoa yksilöllisesti laajentavat tutkinnon osat lukumäärä" in {
        rivi.valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm should equal("1")
      }

      "Korotetut suoritukset" in {
        val rivit = testiHenkilöRaporttiRows(defaultRequest, KoskiSpecificMockOppijat.ammattilainen.hetu.get)

        rivit.length should equal(2)

        rivit(1).suoritettujenOpintojenYhteislaajuus should equal("18.0 (8.0)")
        rivit(1).valmiitAmmatillisetTutkinnonOsatLkm should equal("2 (2)")
        rivit(1).pakollisetAmmatillisetTutkinnonOsatLkm should equal("1 (1)")
        rivit(1).valinnaisetAmmatillisetTutkinnonOsatLkm should equal("1 (1)")
        rivit(1).näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal ("1 (1)")
        rivit(1).tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("1 (1)")
        rivit(1).rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal("1 (1)")
        rivit(1).suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("4.0 (4.0)")
        rivit(1).tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("2.0 (2.0)")
        rivit(1).pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("2.0 (2.0)")
        rivit(1).valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("2.0 (2.0)")
        rivit(1).valmiitYhteistenTutkinnonOsatLkm should equal("1")
        rivit(1).pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal("1 (1)")
        rivit(1).valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal("1 (1)")
        rivit(1).tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal("1 (1)")
        rivit(1).rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal("1 (1)")
        rivit(1).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal("1")
        rivit(1).rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal("1")
        rivit(1).suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("10.0 (10.0)")
        rivit(1).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus should equal("10.0")
        rivit(1).suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("10.0 (10.0)")
        rivit(1).pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("5.0 (5.0)")
        rivit(1).valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal("5.0 (5.0)")
        rivit(1).valmiitVapaaValintaisetTutkinnonOsatLkm should equal("1 (1)")
        rivit(1).valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm should equal("1 (1)")
      }
    }

    "Näyttötutkintoon valmistava koulutus" - {
      "Näytetään erillinen rivi" in {
        val rivit = testiHenkilöRaporttiRows(defaultRequest, KoskiSpecificMockOppijat.erikoisammattitutkinto.hetu.get)

        rivit.length should equal(2)

        rivit(0).opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
        rivit(0).tutkinto should equal("999904")
        rivit(0).osaamisalat should equal(None)
        rivit(0).tutkintonimike should equal("")
        rivit(0).päätasonSuorituksenNimi should equal("Näyttötutkintoon valmistava koulutus")
        rivit(0).päätasonSuorituksenSuoritusTapa should equal("")
        rivit(0).päätasonSuorituksenTila should equal("Valmis")
        rivit(0).viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
        rivit(0).viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("lasna")
        rivit(0).opintojenRahoitukset should equal("1")
        rivit(0).ostettu should equal(false)

        rivit(0).suoritettujenOpintojenYhteislaajuus should equal("0.0")

        rivit(0).valmiitAmmatillisetTutkinnonOsatLkm should equal("0")
        rivit(0).pakollisetAmmatillisetTutkinnonOsatLkm should equal("0")
        rivit(0).valinnaisetAmmatillisetTutkinnonOsatLkm should equal("0")
        rivit(0).näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("0")
        rivit(0).tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("0")
        rivit(0).rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal("0")
        rivit(0).suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(0).tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(0).pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(0).valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")

        rivit(0).valmiitYhteistenTutkinnonOsatLkm should equal("0")
        rivit(0).pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal("0")
        rivit(0).valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal("0")
        rivit(0).tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal("0")
        rivit(0).rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal("0")
        rivit(0).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal("0")
        rivit(0).rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal("0")
        rivit(0).suoritettujenYhteistenTutkinnonOsienYhteislaajuus should equal("0.0")
        rivit(0).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus should equal("0.0")
        rivit(0).suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("0.0")
        rivit(0).pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("0.0")
        rivit(0).valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal("0.0")

        rivit(0).valmiitVapaaValintaisetTutkinnonOsatLkm should equal("0")
        rivit(0).valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm should equal("0")

        rivit(1).opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
        rivit(1).tutkinto should equal("357305")
        rivit(1).osaamisalat should equal(None)
        rivit(1).tutkintonimike should equal("")
        rivit(1).päätasonSuorituksenNimi should equal("Autoalan työnjohdon erikoisammattitutkinto")
        rivit(1).päätasonSuorituksenSuoritusTapa should equal("Näyttötutkinto")
        rivit(1).päätasonSuorituksenTila should equal("Valmis")
        rivit(1).viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
        rivit(1).viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("lasna")
        rivit(1).opintojenRahoitukset should equal("1")
        rivit(1).ostettu should equal(false)

        rivit(1).suoritettujenOpintojenYhteislaajuus should equal("0.0")

        rivit(1).valmiitAmmatillisetTutkinnonOsatLkm should equal("5")
        rivit(1).pakollisetAmmatillisetTutkinnonOsatLkm should equal("5")
        rivit(1).valinnaisetAmmatillisetTutkinnonOsatLkm should equal("0")
        rivit(1).näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("0")
        rivit(1).tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal("0")
        rivit(1).rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal("0")
        rivit(1).suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(1).tunnustetutAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(1).pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")
        rivit(1).valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal("0.0")

        rivit(1).valmiitYhteistenTutkinnonOsatLkm should equal("0")
        rivit(1).pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal("0")
        rivit(1).valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal("0")
        rivit(1).tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal("0")
        rivit(1).rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal("0")
        rivit(1).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal("0")
        rivit(1).rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal("0")
        rivit(1).suoritettujenYhteistenTutkinnonOsienYhteislaajuus should equal("0.0")
        rivit(1).tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaYhteislaajuus should equal("0.0")
        rivit(1).suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("0.0")
        rivit(1).pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal("0.0")
        rivit(1).valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal("0.0")

        rivit(1).valmiitVapaaValintaisetTutkinnonOsatLkm should equal("0")
        rivit(1).valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm should equal("0")
      }

      "Näytetään, vaikka pääsuoritus olisi sisältyvässä opiskeluoikeudessa" in {
        withNewVäärinSiirrettyNäyttötutkintoonValmistavanSisällytettyOpiskeluoikeus {
          val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get

          val rivit = testiHenkilöRaporttiRows(defaultRequest.copy(oppilaitosOid = omnia.oid), KoskiSpecificMockOppijat.erikoisammattitutkinto.hetu.get)

          rivit.length should equal(2)

          rivit(0).opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
          rivit(0).tutkinto should equal("999904")
          rivit(0).osaamisalat should equal(None)
          rivit(0).tutkintonimike should equal("")
          rivit(0).päätasonSuorituksenNimi should equal("Näyttötutkintoon valmistava koulutus")
          rivit(0).päätasonSuorituksenSuoritusTapa should equal("")
          rivit(0).päätasonSuorituksenTila should equal("Valmis")
          rivit(0).viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
          rivit(0).viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("lasna")
          rivit(0).opintojenRahoitukset should equal("1")
          rivit(0).ostettu should equal(false)

          rivit(1).opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
          rivit(1).tutkinto should equal("357305")
          rivit(1).osaamisalat should equal(None)
          rivit(1).tutkintonimike should equal("")
          rivit(1).päätasonSuorituksenNimi should equal("Autoalan työnjohdon erikoisammattitutkinto")
          rivit(1).päätasonSuorituksenSuoritusTapa should equal("Näyttötutkinto")
          rivit(1).päätasonSuorituksenTila should equal("Valmis")
          rivit(1).viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
          rivit(1).viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("valmistunut")
          rivit(1).opintojenRahoitukset should equal("1")
          rivit(1).ostettu should equal(false)
        }
      }
    }

    "Sisällytetyt opiskeluoikeudet" - {
      "Opiskeluoikeuteen sisältyvät opiskeluoikeudet toisesta oppilaitoksesta" in {
        withNewSisällytettyOpiskeluoikeus {
          val aarnenRivit = testiHenkilöRaporttiRows(defaultRequest.copy(oppilaitosOid = MockOrganisaatiot.omnia))
          aarnenRivit.length should equal(2)
          val stadinLinkitettyOpiskeluoikeus = aarnenRivit.find(_.linkitetynOpiskeluoikeudenOppilaitos == "Stadin ammatti- ja aikuisopisto")
          stadinLinkitettyOpiskeluoikeus shouldBe defined
          stadinLinkitettyOpiskeluoikeus.get.suoritettujenOpintojenYhteislaajuus should equal("180.0")
        }
      }
      "Sisältävä opiskeluoikeus ei tule sisällytetyn opiskeluoikeuden oppilaitoksen raportille" in {
        withNewSisällytettyOpiskeluoikeus {
          val rivi = testiHenkilöRaporttiRows(defaultRequest.copy(oppilaitosOid = MockOrganisaatiot.stadinAmmattiopisto))
          rivi.map(_.linkitetynOpiskeluoikeudenOppilaitos) should equal(List(""))
        }
      }
    }
    "Tutkinnon osia voidaan raja arviointipäivän perusteella" - {
      "Tutkinnon osat jotka arvioitu ennen aikaväliä, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(defaultRequest.copy(alku = date(2015, 1, 1), loppu = date(2015, 2, 2), osasuoritustenAikarajaus = true))
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List("40.0"))
      }
      "Tutkinnon osiat jotka arvioitu jälkeen aikavälin, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(defaultRequest.copy(alku = date(2014, 1, 1), loppu = date(2014, 12, 12), osasuoritustenAikarajaus = true))
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List("140.0"))
      }
    }

    "Tutkinnon osien erottelu" - {
      val yhteinenTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "400012", tutkinnonOsanRyhmä = None)
      val ammatillinenTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = None)
      val yhteistenTutkinnonOsienOsaalueitaLukioOpintojaTaiMuitaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = yhteisenTutkinnonOsienOsaalueitaTaiLukioTaiMuitaKoodiarvo, tutkinnonOsanRyhmä = None)
      val korkeakouluopintojaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = korkeakouluopintojaKoodiarvo, tutkinnonOsanRyhmä = None)
      val vapaastiValittavaTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = Some("3"))
      val tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = Some("4"))

      val mahdollisetTutkinnonOsat = List(yhteinenTutkinnonOsaRow, ammatillinenTutkinnonOsaRow, yhteistenTutkinnonOsienOsaalueitaLukioOpintojaTaiMuitaRow, korkeakouluopintojaRow, vapaastiValittavaTutkinnonOsaRow, tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow)

      "Yhteinen tutkinnon osa" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.isYhteinenTutkinnonOsa) should equal(List(
          yhteinenTutkinnonOsaRow
        ))
      }
      "Ammatillinen tutkinnon osa" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.isAmmatillisenTutkinnonOsa) should equal(List(
          ammatillinenTutkinnonOsaRow
        ))
      }
      "Vapaasti valittavat tutkinnon osat" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.tutkinnonOsanRyhmä(_, "3")) should equal(List(
          vapaastiValittavaTutkinnonOsaRow
        ))
      }
      "Tutkintoa yksilöllisesti laajentavat tutkinnon osat" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.tutkinnonOsanRyhmä(_, "4")) should equal(List(
          tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow
        ))
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(
        apiUrl = "api/raportit/ammatillinentutkintosuoritustietojentarkistus",
        expectedRaporttiNimi = AmmatillinenTutkintoSuoritustietojenTarkistus.toString,
        expectedFileNamePrefix = "suoritustiedot"
      )
    }

    "raportin lataaminen toimii eri lokalisaatiolla (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(
        apiUrl = "api/raportit/ammatillinentutkintosuoritustietojentarkistus",
        expectedRaporttiNimi = AmmatillinenTutkintoSuoritustietojenTarkistus.toString,
        expectedFileNamePrefix = "suoritustiedot",
        lang = "sv"
      )
    }
  }

  private val defaultHetu = KoskiSpecificMockOppijat.ammattilainen.hetu.get

  private val defaultRequest = AikajaksoRaporttiAikarajauksellaRequest(
    oppilaitosOid = MockOrganisaatiot.stadinAmmattiopisto,
    alku =  date(2016, 1, 1),
    loppu = date(2016,5 , 30),
    osasuoritustenAikarajaus = false,
    downloadToken = None,
    password = "",
    lang = "fi"
  )

  private def testiHenkilöRaporttiRows(request: AikajaksoRaporttiAikarajauksellaRequest, hetu: String = defaultHetu): Seq[SuoritustiedotTarkistusRow] =
    AmmatillinenTutkintoRaportti.buildRaportti(request, repository, t).filter(_.hetu.contains(hetu)).toList

  private def withNewSisällytettyOpiskeluoikeus(f: => Unit) = {
    resetFixtures
    val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get
    val omnianOpiskeluoikeus = makeOpiskeluoikeus(date(2016, 1, 1), omnia, omnia.oid)
    val oppija = KoskiSpecificMockOppijat.ammattilainen

    putOpiskeluoikeus(omnianOpiskeluoikeus, oppija){}

    val stadinOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).find(_.oppilaitos.map(_.oid).contains(MockOrganisaatiot.stadinAmmattiopisto)).map{case oo: AmmatillinenOpiskeluoikeus => oo}.get
    val omnianOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammattilainen.oid).oid.get

    putOpiskeluoikeus(sisällytäOpiskeluoikeus(stadinOpiskeluoikeus, SisältäväOpiskeluoikeus(omnia, omnianOpiskeluoikeusOid)), oppija){}
    reloadRaportointikanta
    (f)
  }

  private def withNewVäärinSiirrettyNäyttötutkintoonValmistavanSisällytettyOpiskeluoikeus(f: => Unit) = {
    // Stadin opiskeluoikeus (jossa päätutkinto) sisältyy omnian opiskeluoikeuteen (jossa pelkkä näyttötutkintoon valmistava)
    resetFixtures
    val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get

    val omnianOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
      arvioituPäättymispäivä = Some(date(2015, 5, 31)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), ExampleData.opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      oppilaitos = Some(omnia),
      suoritukset = List(
        AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus
      )
    )

    val oppija = KoskiSpecificMockOppijat.erikoisammattitutkinto

    putOpiskeluoikeus(omnianOpiskeluoikeus, oppija){}

    val stadinOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
      arvioituPäättymispäivä = Some(date(2015, 5, 31)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), ExampleData.opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )),
      oppilaitos = Some(AmmatillinenExampleData.stadinAmmattiopisto),
      suoritukset = List(
        AmmattitutkintoExample.ammatillisenTutkinnonSuoritus.copy(osasuoritukset = Some(List(
          AmmatillinenExampleData.tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", None, AmmatillinenExampleData.hyväksytty),
          AmmatillinenExampleData.tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", None, AmmatillinenExampleData.hyväksytty),
          AmmatillinenExampleData.tutkinnonOsanSuoritus("104059", "Yrittäjyys", None, AmmatillinenExampleData.hyväksytty)
        )))
      )
    )

    val omnianOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.erikoisammattitutkinto.oid).oid.get

    putOpiskeluoikeus(sisällytäOpiskeluoikeus(stadinOpiskeluoikeus, SisältäväOpiskeluoikeus(omnia, omnianOpiskeluoikeusOid)), oppija){}
    reloadRaportointikanta
    (f)
  }

  private def osasuoritusRow(suorituksenTyyppi: String, koulutusmoduulikoodiarvo: String, tutkinnonOsanRyhmä: Option[String]) = {
    ROsasuoritusRow(
      osasuoritusId = 1L,
      ylempiOsasuoritusId = None,
      päätasonSuoritusId = 1L,
      opiskeluoikeusOid = "1",
      suorituksenTyyppi = suorituksenTyyppi,
      koulutusmoduuliKoodiarvo = koulutusmoduulikoodiarvo,
      koulutusmoduuliPaikallinen = false,
      koulutusmoduuliKoodisto = None,
      koulutusmoduuliLaajuusArvo = None,
      koulutusmoduuliLaajuusYksikkö = None,
      koulutusmoduuliPakollinen = None,
      koulutusmoduuliNimi = None,
      koulutusmoduuliOppimääräNimi = None,
      koulutusmoduuliKieliaineNimi = None,
      koulutusmoduuliKurssinTyyppi = None,
      vahvistusPäivä = None,
      arviointiArvosanaKoodiarvo = None,
      arviointiArvosanaKoodisto = None,
      arviointiHyväksytty = None,
      arviointiPäivä = None,
      ensimmäinenArviointiPäivä = None,
      korotettuEriVuonna = false,
      näytönArviointiPäivä = None,
      tunnustettu = false,
      tunnustettuRahoituksenPiirissä = false,
      data = mockJValueData(tutkinnonOsanRyhmä),
      sisältyyOpiskeluoikeuteenOid = None
    )
  }

  private def mockJValueData(tutkinnonOsanRyhmäKoodiarvo: Option[String]) = {
    implicit val user = SensitiveDataAllowed.SystemUser
    JsonSerializer.serialize(Map(
      "tutkinnonOsanRyhmä" -> tutkinnonOsanRyhmäKoodiarvo.map(Koodistokoodiviite(_, "ammatillisentutkinnonosanryhma"))
    ))
  }

  private lazy val ammatillinenTutkinnonOsa = "ammatillisentutkinnonosa"
  private lazy val yhteisenTutkinnonOsienOsaalueitaTaiLukioTaiMuitaKoodiarvo = "1"
  private lazy val korkeakouluopintojaKoodiarvo = "2"
}

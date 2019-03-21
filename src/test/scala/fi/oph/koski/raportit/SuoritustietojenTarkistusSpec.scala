package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, SisältäväOpiskeluoikeus}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class SuoritustietojenTarkistusSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {
  "Suoritustietojen tarkistusraportti" - {
    loadRaportointikantaFixtures
    val rivit = loadAmmattilaisAarnenRivit()
    val rivi = rivit.head

    "Sisältää oikeat tiedot" in {
      rivi.koulutusmoduulit should equal("361902")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.opiskeluoikeudenTila should equal(Some("Valmis"))
      rivi.opintojenRahoitukset should equal("4")
      rivi.ostettu should equal(false)
    }

    "Laskenta" - {
      "Suorituksia yhteesä" in {
        rivi.suoritettujenOpintojenYhteislaajuus should equal(157.0)
      }
      "Ammatilliset tutkinnon osat" - {
        "Valmiiden ammatillisten tutkinnon osien lukumäärä" in {
          rivi.valmiitAmmatillisetTutkinnonOsatLkm should equal(7)
        }
        "Pakolliset ammatilliset tutkinnon osat" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatLkm should equal(6)
        }
        "Valinnaiset ammatilliset tutkinnon osat" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatLkm should equal(1)
        }
        "Näyttöjä ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal(3)
        }
        "Tunnustettuja ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal(0)
        }
        "Rahoituksen piirissä tunnustettuja ammatillisia tutkinnon osia" in {
          rivi.rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal(0)
        }
        "Suoritetut ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal(135.0)
        }
        "Pakolliset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal(135.0)
        }
        "Valinnaiset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal(0)
        }
      }
      "Yhteiset tutkinnon osat" - {
        "Valmiit yhteiset tutkinnon osat lukumäärä" in {
          rivi.valmiitYhteistenTutkinnonOsatLkm should equal(4)
        }
        "Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal(7)
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal(1)
        }
        "Tunnustettuja yhteisten tutkinnon osan osa-alueita valmiista yhteisen tutkinnon osa-alueista" in {
          rivi.tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal(1)
        }
        "Rahoituksen piirissä tutkinnon osan osa-alueita valmiissa yhteisten tutkinnon osan osa-aluiesta" in {
          rivi.rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal(0)
        }
        "Tunnustettuja tutkinnon osia valmiista yhteisen tutkinnon osista" in {
          rivi.tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal(0)
        }
        "Rahoituksen piirissä tunnustetuista yhteisistä tutkinnon osista" in {
          rivi.rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal(0)
        }
        "Suoritettuja yhteisten tutkinnon osien yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienYhteislaajuus should equal(35.0)
        }
        "Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal(22)
        }
        "Pakollisten yhteisten tutkinnon osioen osa-alueiden yhteislaajuus" in {
          rivi.pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal(19)
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal(3)
        }
      }
    }
    "Sisällytetyt opiskeluoikeudet"  - {
      "Opiskeluoikeuteen sisältyvät opiskeluioikeudet toistesta oppilaitoksesta" in {
        withNewSisällytettyOpiskeluoikeus {
          val aarnenRivit = loadAmmattilaisAarnenRivit(MockOrganisaatiot.omnia)
          aarnenRivit.length should equal(2)
          val stadinLinkitettyOpiskeluoikeus = aarnenRivit.find(_.linkitetynOpiskeluoikeudenOppilaitos == stadinAmmattiOpistonNimi)
          stadinLinkitettyOpiskeluoikeus shouldBe defined
          stadinLinkitettyOpiskeluoikeus.get.suoritettujenOpintojenYhteislaajuus should equal(157.0)
        }
      }
      "Sisältävä opiskeluoikeus ei tule sisällytetyn opiskeluoikeuden oppilaitoksen raportille" in {
        withNewSisällytettyOpiskeluoikeus {
          val aarnenRivit = loadAmmattilaisAarnenRivit(MockOrganisaatiot.stadinAmmattiopisto)
          aarnenRivit.length should equal(1)
          aarnenRivit.head.linkitetynOpiskeluoikeudenOppilaitos shouldBe empty
        }
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/suoritustietojentarkistus", expectedRaporttiNimi = "suoritustietojentarkistus")
    }
  }

  override def beforeAll(): Unit = loadRaportointikantaFixtures

  private def loadAmmattilaisAarnenRivit(oppilaitosOid: String = MockOrganisaatiot.stadinAmmattiopisto) = {
    val result = SuoritustietojenTarkistus.buildRaportti(KoskiApplicationForTests.raportointiDatabase, oppilaitosOid, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))
    result.filter(_.hetu == MockOppijat.ammattilainen.hetu)
  }

  private def withNewSisällytettyOpiskeluoikeus(f: => Unit) = {
    resetFixtures
    val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get
    val omnianOpiskeluoikeus = makeOpiskeluoikeus(LocalDate.of(2016, 1, 1), omnia, omnia.oid)
    val oppija = MockOppijat.ammattilainen

    putOpiskeluoikeus(omnianOpiskeluoikeus, oppija){}

    val stadinOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).find(_.oppilaitos.map(_.oid).contains(MockOrganisaatiot.stadinAmmattiopisto)).map{case oo: AmmatillinenOpiskeluoikeus => oo}.get
    val omnianOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ammattilainen.oid).oid.get

    putOpiskeluoikeus(sisällytäOpiskeluoikeus(stadinOpiskeluoikeus, SisältäväOpiskeluoikeus(omnia, omnianOpiskeluoikeusOid)), oppija){}
    loadRaportointikantaFixtures
    (f)
  }

  private val stadinAmmattiOpistonNimi = MockOrganisaatioRepository.getOrganisaatio(MockOrganisaatiot.stadinAmmattiopisto).get.nimi.get.values("fi")
}

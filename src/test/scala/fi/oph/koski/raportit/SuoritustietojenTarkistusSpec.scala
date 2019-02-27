package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{FreeSpec, Matchers}

class SuoritustietojenTarkistusSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethods {

  "Suoritustietojen tarkistusraportti" - {
    "Luo raportin" - {
      loadRaportointikantaFixtures
      val result = SuoritustietojenTarkistus.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))
      val aarnenOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ammattilainen.oid).oid.get
      val aarnenRivi = result.find(_.opiskeluoikeusOid == aarnenOpiskeluoikeusOid)
      aarnenRivi shouldBe defined
      val rivi = aarnenRivi.get

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
        "Pakollisten yhteisten tutkinnon osioen osa-alueiden yhteislaajuus" in {
          rivi.pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal(19)
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal(3)
        }
      }
    }
  }
}

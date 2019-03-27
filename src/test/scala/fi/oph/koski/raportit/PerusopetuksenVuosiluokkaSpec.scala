package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{FreeSpec, Matchers}

class PerusopetuksenVuosiluokkaSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsPerusopetus {

  resetFixtures

  "Perusopetuksenvuosiluokka raportti" - {
    loadRaportointikantaFixtures

    "Tuottaa oikeat tiedot" in {
      val result = PerusopetuksenVuosiluokka.buildRaportti(KoskiApplicationForTests.raportointiDatabase, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2014, 1, 1), LocalDate.of(2015, 12, 12), vuosiluokka = "8")
      val ynjevi = MockOppijat.ysiluokkalainen
      val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(ynjevi.oid).oid.get
      val ynjevinRivi = result.find(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)
      ynjevinRivi shouldBe defined

      val rivi = ynjevinRivi.get
      rivi should equal(
        PerusopetusRow(
          opiskeluoikeusOid = ynjevinOpiskeluoikeusOid,
          lähdejärjestelmä = None,
          lähdejärjestelmänId = None,
          oppijaOid = ynjevi.oid,
          hetu = ynjevi.hetu,
          sukunimi = Some(ynjevi.sukunimi),
          etunimet = Some(ynjevi.etunimet),
          luokka = "8C",
          viimeisinTila = "lasna",
          aidinkieli = "9",
          kieliA = "8",
          kieliB = "8",
          uskonto = "10",
          historia = "8",
          yhteiskuntaoppi = "10",
          matematiikka = "9",
          kemia = "7",
          fysiikka = "9",
          biologia = "9",
          maantieto = "9",
          musiikki = "7",
          kuvataide = "8",
          kotitalous = "8",
          terveystieto = "8",
          kasityo = "9",
          liikunta = "9",
          ymparistooppi = "Arvosana puuttuu",
          paikallistenOppiaineidenKoodit = "TH",
          pakollisetPaikalliset = "",
          valinnaisetPaikalliset = "Tietokoneen hyötykäyttö (TH)",
          valinnaisetValtakunnalliset = "ruotsi (B1),Kotitalous (KO),Liikunta (LI),saksa (B2)",
          valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "saksa (B2)",
          valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "ruotsi (B1),Kotitalous (KO),Liikunta (LI)",
          numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = ""
        )
      )
    }
  }
}

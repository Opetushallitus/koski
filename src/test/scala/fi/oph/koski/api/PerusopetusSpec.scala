package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import org.scalatest.{FunSpec, Matchers}

class PerusopetusSpec extends FunSpec with Matchers with TodistusTestMethods with OpiskeluOikeusTestMethods {
  describe("Perusopetuksen todistukset") {
    it("Perusopetuksen päättötodistus") {
      resetFixtures
      todistus(MockOppijat.koululainen.oid, "perusopetus", Some("koulutus/201101")) should equal(
        """|Jyväskylän yliopisto
          |Perusopetuksen päättötodistus
          |Jyväskylän normaalikoulu
          |Koululainen, Kaisa 110496-926Y
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9
          |B1-kieli, ruotsi Hyvä 8
          |Valinnainen b1-kieli, ruotsi 1.0 Hyväksytty
          |A1-kieli, englanti Hyvä 8
          |Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto Erinomainen 10
          |Historia Hyvä 8
          |Yhteiskuntaoppi Erinomainen 10
          |Matematiikka Kiitettävä 9
          |Kemia Tyydyttävä 7
          |Fysiikka Kiitettävä 9
          |Biologia Kiitettävä 9
          |Maantieto Kiitettävä 9
          |Musiikki Tyydyttävä 7
          |Kuvataide Hyvä 8
          |Kotitalous Hyvä 8
          |Valinnainen kotitalous 1.0 Hyväksytty
          |Terveystieto Hyvä 8
          |Käsityö Kiitettävä 9
          |Liikunta Kiitettävä 9
          |Valinnainen liikunta 0.5 Hyväksytty
          |
          |B2-kieli, saksa 4.0 Kiitettävä 9""".stripMargin)
    }
    it("Perusopetuksen päättötodistus toiminta-alueittain, sanallisella arvioinnilla") {
      todistus(MockOppijat.toimintaAlueittainOpiskelija.oid, "perusopetus") should equal(
        """Jyväskylän yliopisto
          |Perusopetuksen päättötodistus
          |Jyväskylän normaalikoulu
          |Toiminta, Tommi 130696-913E
          |
          |motoriset taidot Motoriset taidot kehittyneet hyvin perusopetuksen aikana
          |kieli ja kommunikaatio Hyväksytty
          |sosiaaliset taidot Hyväksytty
          |päivittäisten toimintojen taidot Hyväksytty
          |kognitiiviset taidot Hyväksytty""".stripMargin)
    }

    it("Perusopetuksen oppiaineen oppimäärän todistus") {
      todistus(MockOppijat.oppiaineenKorottaja.oid, "perusopetus") should equal(
        """Jyväskylän yliopisto
          |Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta
          |Jyväskylän normaalikoulu
          |Oppiaineenkorottaja, Olli 190596-953T
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9""".stripMargin)
    }

    it("Perusopetuksen lukuvuositodistus") {
      todistus(MockOppijat.koululainen.oid, "perusopetus", Some("perusopetuksenluokkaaste/8")) should equal(
        """Jyväskylän yliopisto
          |Lukuvuositodistus - 8. vuosiluokka
          |Jyväskylän normaalikoulu
          |Koululainen, Kaisa 110496-926Y
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9
          |B1-kieli, ruotsi Hyvä 8
          |Valinnainen b1-kieli, ruotsi 1.0 Hyväksytty
          |A1-kieli, englanti Hyvä 8
          |Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto Erinomainen 10
          |Historia Hyvä 8
          |Yhteiskuntaoppi Erinomainen 10
          |Matematiikka Kiitettävä 9
          |Kemia Tyydyttävä 7
          |Fysiikka Kiitettävä 9
          |Biologia Kiitettävä 9
          |Maantieto Kiitettävä 9
          |Musiikki Tyydyttävä 7
          |Kuvataide Hyvä 8
          |Kotitalous Hyvä 8
          |Valinnainen kotitalous 1.0 Hyväksytty
          |Terveystieto Hyvä 8
          |Käsityö Kiitettävä 9
          |Liikunta Kiitettävä 9
          |Valinnainen liikunta 0.5 Hyväksytty
          |
          |B2-kieli, saksa 4.0 Kiitettävä 9""".stripMargin)

    }

  }
  describe("Perusopetuksen lisäopetus") {
    it("todistus") {
      todistus(MockOppijat.kymppiluokkalainen.oid, "perusopetuksenlisaopetus") should equal(
        """|Jyväskylän yliopisto
          |Todistus lisäopetuksen suorittamisesta
          |Jyväskylän normaalikoulu
          |Kymppiluokkalainen, Kaisa 200596-9755
          |
          |Äidinkieli ja kirjallisuus Tyydyttävä 7
          |A1-kieli, englanti Erinomainen 10
          |B1-kieli, ruotsi Kohtalainen 6
          |Matematiikka Kohtalainen 6
          |Biologia Erinomainen 10
          |Maantieto Kiitettävä 9
          |Fysiikka Hyvä 8
          |Kemia Kiitettävä 9
          |Terveystieto Hyvä 8
          |Historia Tyydyttävä 7
          |Yhteiskuntaoppi Hyvä 8
          |Kuvataide Hyvä 8
          |Liikunta Tyydyttävä 7""".stripMargin)
    }
  }

}


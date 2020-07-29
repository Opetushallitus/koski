package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class PerusopetusSpec extends FreeSpec with Matchers with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "Perusopetuksen todistukset" - {
    "Perusopetuksen päättötodistus" in {
      resetFixtures
      todistus(MockOppijat.koululainen.oid, "perusopetuksenoppimaara", Some("koulutus/201101")) should equal(
        """|Jyväskylän yliopisto
          |Perusopetuksen päättötodistus
          |Jyväskylän normaalikoulu
          |Koululainen, Kaisa 220109-784L
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9
          |B1-kieli, ruotsi Hyvä 8
          |Valinnainen b1-kieli, ruotsi 1.0 Hyväksytty
          |A1-kieli, englanti Hyvä 8
          |Uskonto/Elämänkatsomustieto Erinomainen 10
          |Historia Hyvä 8
          |Yhteiskuntaoppi Erinomainen 10
          |Matematiikka Kiitettävä 9
          |Kemia Tyydyttävä 7
          |Fysiikka Kiitettävä 9
          |Biologia Kiitettävä 9 *
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
          |B2-kieli, saksa 4.0 Kiitettävä 9
          |Tietokoneen hyötykäyttö Kiitettävä 9
          |Oppilas on opiskellut tähdellä (*) merkityt oppiaineet yksilöllistetyn oppimäärän mukaan.""".stripMargin)
    }
    "Perusopetuksen päättötodistus toiminta-alueittain, sanallisella arvioinnilla" in {
      todistus(MockOppijat.toimintaAlueittainOpiskelija.oid, "perusopetuksenoppimaara") should equal(
        """Jyväskylän yliopisto
          |Perusopetuksen päättötodistus
          |Jyväskylän normaalikoulu
          |Toiminta, Tommi 031112-020J
          |
          |motoriset taidot Motoriset taidot kehittyneet hyvin perusopetuksen aikana
          |kieli ja kommunikaatio Hyväksytty
          |sosiaaliset taidot Hyväksytty
          |päivittäisten toimintojen taidot Hyväksytty
          |kognitiiviset taidot Hyväksytty""".stripMargin)
    }

    "Perusopetuksen oppiaineen oppimäärän todistus" in {
      todistus(MockOppijat.oppiaineenKorottaja.oid, "perusopetuksenoppiaineenoppimaara") should equal(
        """Jyväskylän yliopisto
          |Todistus perusopetuksen oppiaineen oppimäärän suorittamisesta
          |Jyväskylän normaalikoulu
          |Oppiaineenkorottaja, Olli 110738-839L
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9""".stripMargin)
    }

    "Perusopetuksen lukuvuositodistus" in {
      todistus(MockOppijat.koululainen.oid, "perusopetuksenvuosiluokka", Some("perusopetuksenluokkaaste/8")) should equal(
        """Jyväskylän yliopisto
          |Lukuvuositodistus - 8. vuosiluokka
          |Jyväskylän normaalikoulu
          |Koululainen, Kaisa 220109-784L 8C
          |
          |Äidinkieli ja kirjallisuus Kiitettävä 9
          |B1-kieli, ruotsi Hyvä 8
          |Valinnainen b1-kieli, ruotsi 1.0 Hyväksytty
          |A1-kieli, englanti Hyvä 8
          |Uskonto/Elämänkatsomustieto Erinomainen 10
          |Historia Hyvä 8
          |Yhteiskuntaoppi Erinomainen 10
          |Matematiikka Kiitettävä 9
          |Kemia Tyydyttävä 7
          |Fysiikka Kiitettävä 9
          |Biologia Kiitettävä 9 *
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
          |B2-kieli, saksa 4.0 Kiitettävä 9
          |Tietokoneen hyötykäyttö Kiitettävä 9
          |Oppilas on opiskellut tähdellä (*) merkityt oppiaineet yksilöllistetyn oppimäärän mukaan.""".stripMargin)
    }

  }
  "Perusopetuksen lisäopetus" - {
    "todistus" in {
      todistus(MockOppijat.kymppiluokkalainen.oid, "perusopetuksenlisaopetus") should equal(
        """|Jyväskylän yliopisto
          |Todistus lisäopetuksen suorittamisesta
          |Jyväskylän normaalikoulu
          |Kymppiluokkalainen, Kaisa 131025-6573
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
          |Liikunta Tyydyttävä 7 *
          |Oppilas on opiskellut tähdellä (*) merkityt oppiaineet yksilöllistetyn oppimäärän mukaan.""".stripMargin)
    }
  }

  "Perusopetukseen valmistava opetus" - {
    "todistus" in {
      todistus(MockOppijat.koululainen.oid, "perusopetukseenvalmistavaopetus") should equal (
        """Todistus perusopetukseen valmistavaan opetukseen osallistumisesta
          |Jyväskylän yliopisto
          |Jyväskylän normaalikoulu
          |Koululainen, Kaisa 220109-784L
          |
          |Äidinkieli Suullinen ilmaisu ja kuullun ymmärtäminen 10 Keskustelee sujuvasti suomeksi
          |Fysiikka""".stripMargin
      )
    }
  }

}


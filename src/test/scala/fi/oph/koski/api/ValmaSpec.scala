package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class ValmaSpec extends FreeSpec with Matchers with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)" - {
    "Päättötodistus" in {
      todistus(MockOppijat.valma.oid, "valma") should equal(
        """Ammatilliseen peruskoulutukseen valmentava koulutus
          |Helsingin kaupunki
          |Stadin ammattiopisto
          |Amikseenvalmistautuja, Anneli 130404-054C
          |
          |Pakolliset koulutuksen osat 18 osp
          |Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen 10 Hyväksytty
          |Äidinkieli, Suomen kieli ja kirjallisuus 5 Kiitettävä 3
          |Toinen kotimainen kieli, ruotsi, ruotsi 1 Kiitettävä 3
          |Vieraat kielet, englanti 2 Kiitettävä 3
          |Valinnaiset koulutuksen osat 58 osp
          |Opiskeluvalmiuksien vahvistaminen 10 Hyväksytty
          |Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen 15 Hyväksytty
          |Arjen taitojen ja hyvinvoinnin vahvistaminen 10 Hyväksytty
          |Tietokoneen käyttäjän AB-kortti 5 Hyväksytty
          |Auton lisävarustetyöt 1) 15 Hyväksytty
          |Äidinkieli, Suomen kieli ja kirjallisuus 3 Kiitettävä 3
          |Opiskelijan suorittamien koulutuksen osien laajuus osaamispisteinä 76
          |Lisätietoja:
          |1)Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta (3.10.2015, 39/011/2014), Stadin ammattiopisto""".stripMargin)
    }
  }
}

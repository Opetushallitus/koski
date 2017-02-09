package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class ValmaSpec extends FreeSpec with Matchers with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)" - {
    "Päättötodistus" in {
      todistus(MockOppijat.valma.oid, "valma") should equal(
        """Ammatilliseen peruskoulutukseen valmentava koulutus
          |HELSINGIN KAUPUNKI
          |Stadin ammattiopisto
          |Amikseenvalmistautuja, Anneli 130404-054C
          |
          |Pakolliset koulutuksen osat 10 osp
          |Ammatilliseen koulutukseen orientoituminen ja työelämän perusvalmiuksien hankkiminen 10 Hyväksytty
          |Valinnaiset koulutuksen osat 50 osp
          |Opiskeluvalmiuksien vahvistaminen 10 Hyväksytty
          |Työssäoppimiseen ja oppisopimuskoulutukseen valmentautuminen 15 Hyväksytty
          |Arjen taitojen ja hyvinvoinnin vahvistaminen 10 Hyväksytty
          |Auton lisävarustetyöt 1) 15 Hyväksytty
          |Opiskelijan suorittamien koulutuksen osien laajuus osaamispisteinä 60
          |Lisätietoja:
          |1)Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta (3.10.2015, 39/011/2014), Stadin ammattiopisto""".stripMargin)
    }
  }
}

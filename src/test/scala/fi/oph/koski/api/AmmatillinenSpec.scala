package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.FreeSpec

class AmmatillinenSpec extends FreeSpec with TodistusTestMethods with LocalJettyHttpSpecification {
  "Ammatillisen perustutkinnon päättötodistus" in {
    todistus(MockOppijat.ammattilainen.oid, "ammatillinentutkinto") should equal(
      """Helsingin kaupunki
        |Stadin ammattiopisto
        |Päättötodistus
        |Luonto- ja ympäristöalan perustutkinto
        |Ympäristöalan osaamisala, Ympäristönhoitaja
        |Ammattilainen, Aarne (280618-402H)
        |
        |Ammatilliset tutkinnon osat
        |Kestävällä tavalla toimiminen 40 Kiitettävä 3
        |Ympäristön hoitaminen 35 Kiitettävä 3
        |Uusiutuvien energialähteiden hyödyntäminen 15 Kiitettävä 3
        |Ulkoilureittien rakentaminen ja hoitaminen 15 Kiitettävä 3
        |Kulttuuriympäristöjen kunnostaminen ja hoitaminen 15 Kiitettävä 3
        |Vesistöjen kunnostaminen ja hoitaminen 15 Hyväksytty M)
        |Yhteiset tutkinnon osat
        |Viestintä- ja vuorovaikutusosaaminen 11 Kiitettävä 3
        |Matemaattis-luonnontieteellinen osaaminen 9 Kiitettävä 3 M1)
        |Yhteiskunnassa ja työelämässä tarvittava osaaminen 8 Kiitettävä 3
        |Sosiaalinen ja kulttuurinen osaaminen 7 Kiitettävä 3
        |Vapaasti valittavat tutkinnon osat
        |Sosiaalinen ja kulttuurinen osaaminen 5 Kiitettävä 3
        |Tutkintoa yksilöllisesti laajentavat tutkinnon osat
        |Matkailuenglanti 5 Kiitettävä 3
        |Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä 180
        |Tutkintoon sisältyy
        |Työssäoppimisen kautta hankittu osaaminen (5.0 osp)
        |M) Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.
        |M1) Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella""".stripMargin)
  }

  "Näyttötutkintoon valmistava koulutus" in {
    resetFixtures
    todistus(MockOppijat.erikoisammattitutkinto.oid, "nayttotutkintoonvalmistavakoulutus") should equal(
      """Stadin ammattiopisto
        |Näyttötutkintoon valmistavan koulutuksen osallistumistodistus
        |Erikoinen, Erja 250989-419V
        |on osallistunut Autoalan työnjohdon erikoisammattitutkinto, valmistavaan koulutukseen 1.9.2012 seuraavilta osin:
        |Koulutuksen sisällöt
        |Johtaminen ja henkilöstön kehittäminen
        |Auton lisävarustetyöt
        |Auton lisävarustetyöt
        |Tutkintotodistuksen saamiseksi on osoitettava tukinnon perusteissa edellytetty ammattitaito tutkintotilaisuuksissa tutkintotoimikunnan valvonnassa. Tutkintotoimikunta antaa tutkintotodistuksen erikseen.""".stripMargin)
  }
}

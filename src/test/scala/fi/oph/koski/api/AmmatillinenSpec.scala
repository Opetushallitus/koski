package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import org.scalatest.FreeSpec

class AmmatillinenSpec extends FreeSpec with TodistusTestMethods with LocalJettyHttpSpecification {
  "Ammatillisen perustutkinnon päättötodistus" in {
    todistus(MockOppijat.ammattilainen.oid, "ammatillinentutkinto") should equal("""HELSINGIN KAUPUNKI
                                                                                   |Stadin ammattiopisto
                                                                                   |Päättötodistus
                                                                                   |Luonto- ja ympäristöalan perustutkinto
                                                                                   |Ympäristöalan osaamisala, Ympäristönhoitaja
                                                                                   |Ammattilainen, Aarne (120496-949B)
                                                                                   |
                                                                                   |Ammatilliset tutkinnon osat
                                                                                   |Kestävällä tavalla toimiminen 40 Kiitettävä 3
                                                                                   |Ympäristön hoitaminen 35 Kiitettävä 3
                                                                                   |Uusiutuvien energialähteiden hyödyntäminen 15 Kiitettävä 3
                                                                                   |Ulkoilureittien rakentaminen ja hoitaminen 15 Kiitettävä 3
                                                                                   |Kulttuuriympäristöjen kunnostaminen ja hoitaminen 15 Kiitettävä 3
                                                                                   |Vesistöjen kunnostaminen ja hoitaminen 15 Kiitettävä 3
                                                                                   |Yhteiset tutkinnon osat
                                                                                   |Viestintä- ja vuorovaikutusosaaminen 11 Kiitettävä 3
                                                                                   |Matemaattis-lunnontieteellinen osaaminen 9 Kiitettävä 3
                                                                                   |Yhteiskunnassa ja työelämässä tarvittava osaaminen 8 Kiitettävä 3
                                                                                   |Sosiaalinen ja kulttuurinen osaaminen 7 Kiitettävä 3
                                                                                   |Vapaasti valittavat tutkinnon osat
                                                                                   |Tutkintoa yksilöllisesti laajentavat tutkinnon osat
                                                                                   |Matkailuenglanti 5 Kiitettävä 3
                                                                                   |Sosiaalinen ja kulttuurinen osaaminen 5 Kiitettävä 3
                                                                                   |Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä 180
                                                                                   |Tutkintoon sisältyy
                                                                                   |Työssäoppimisen kautta hankittu osaaminen (5.0 osp)""".stripMargin)
  }

  "Näyttötutkintoon valmistava koulutus" in {
    resetFixtures
    todistus(MockOppijat.erikoisammattitutkinto.oid, "nayttotutkintoonvalmistavakoulutus") should equal("""Stadin ammattiopisto
                                                                                                          |Näyttötutkintoon valmistavan koulutuksen osallistumistodistus
                                                                                                          |Erikoinen, Erja 200696-906R
                                                                                                          |on osallistunut Autoalan työnjohdon erikoisammattitutkinto, valmistavaan koulutukseen 1.9.2012 seuraavilta osin:
                                                                                                          |Koulutuksen sisällöt
                                                                                                          |Johtaminen ja henkilöstön kehittäminen
                                                                                                          |Tutkintotodistuksen saamiseksi on osoitettava tukinnon perusteissa edellytetty ammattitaito tutkintotilaisuuksissa tutkintotoimikunnan valvonnassa. Tutkintotoimikunta antaa tutkintotodistuksen erikseen.""".stripMargin)
  }
}

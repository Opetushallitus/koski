package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class TelmaSpec extends FreeSpec with Matchers with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)" - {
    "Päättötodistus" in {
      todistus(MockOppijat.telma.oid, "telma") should equal(
        """Työhön ja itsenäiseen elämään valmentava koulutus
          |HELSINGIN KAUPUNKI
          |Stadin ammattiopisto
          |Telmanen, Tuula 021080-725C
          |
          |Pakolliset koulutuksen osat 53 osp
          |Toimintakyvyn vahvistaminen 18 Opiskelija selviytyy arkielämään liittyvistä toimista, osaa hyödyntää apuvälineitä, palveluita ja tukea sekä on valinnut itselleen sopivan tavan viettää vapaa-aikaa.
          |Opiskeluvalmiuksien vahvistaminen 15 Opiskelija osaa opiskella työskennellä itsenäisesti, mutta ryhmässä toimimisessa tarvitsee joskus apua. Hän viestii vuorovaikutustilanteissa hyvin, osaa käyttää tietotekniikkaa ja matematiikan perustaitoja arkielämässä.
          |Työelämään valmentautuminen 20 Opiskelijalla on käsitys itsestä työntekijänä, mutta työyhteisön säännöt vaativat vielä harjaantumista.
          |Valinnaiset koulutuksen osat 22 osp
          |Tieto- ja viestintätekniikka sekä sen hyödyntäminen 1) 2 Hyväksytty
          |Uimaliikunta ja vesiturvallisuus 2) 5 Hyvä 2
          |Auton lisävarustetyöt 15 Hyväksytty
          |Opiskelijan suorittamien koulutuksen osien laajuus osaamispisteinä 75
          |Lisätietoja:
          |1)Yhteisten tutkinnon osien osa-alue on suoritettu x- perustutkinnon perusteiden (2015) osaamistavoitteiden mukaisesti
          |2)Koulutuksen osa on tunnustettu Vesikallion urheiluopiston osaamistavoitteiden mukaisesti""".stripMargin)
    }
  }
}


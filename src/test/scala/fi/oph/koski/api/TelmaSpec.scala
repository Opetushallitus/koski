package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import org.scalatest.{FunSpec, Matchers}

class TelmaSpec extends FunSpec with Matchers with TodistusTestMethods with OpiskeluOikeusTestMethods {
  describe("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)") {
    it("Päättötodistus") {
      todistus(MockOppijat.telma.oid, "telma") should equal(
        """Työhön ja itsenäiseen elämään valmentava koulutus
          |Jyväskylän yliopisto
          |Jyväskylän normaalikoulu
          |Telmanen, Tuula 170696-986C
          |
          |Pakolliset koulutuksen osat 53 osp
          |Toimintakyvyn vahvistaminen 18 Opiskelija selviytyy arkielämään liittyvistä toimista, osaa hyödyntää apuvälineitä, palveluita ja tukea sekä on valinnut itselleen sopivan tavan viettää vapaa-aikaa.
          |Opiskeluvalmiuksien vahvistaminen 15 Opiskelija osaa opiskella työskennellä itsenäisesti, mutta ryhmässä toimimisessa tarvitsee joskus apua. Hän viestii vuorovaikutustilanteissa hyvin, osaa käyttää tietotekniikkaa ja matematiikan perustaitoja arkielämässä.
          |Työelämään valmentautuminen 20 Opiskelijalla on käsitys itsestä työntekijänä, mutta työyhteisön säännöt vaativat vielä harjaantumista.
          |
          |Valinnaiset koulutuksen osat 20 osp
          |Tieto- ja viestintätekniikka sekä sen hyödyntäminen 2 tyydyttävä 1
          |Uimaliikunta ja vesiturvallisuus 5 hyväksytty""".stripMargin)
    }
  }
}


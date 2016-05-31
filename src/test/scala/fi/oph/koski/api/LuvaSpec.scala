package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import org.scalatest.{Matchers, FunSpec}

class LuvaSpec extends FunSpec with Matchers with TodistusTestMethods {
  describe("Lukioon valmistava koulutus (LUVA)") {
    it("Päättötodistus") {
      todistus(MockOppijat.luva.hetu) should equal("""Lukioon valmistavan koulutuksen päättötodistus
                                                     |Jyväskylän yliopisto
                                                     |Jyväskylän normaalikoulu
                                                     |Lukioonvalmistautuja, Luke 300596-9615
                                                     |
                                                     |Lukioon valmistavat opinnot 4
                                                     |Suomi toisena kielenä ja kirjallisuus 2 Hyväksytty
                                                     |Yhteiskuntatietous ja kulttuurintuntemus 1 Hyväksytty
                                                     |Opinto-ohjaus 1 Hyväksytty
                                                     |Valinnaisena suoritetut lukiokurssit 1
                                                     |Kuvat ja kulttuurit 1 Tyydyttävä 7
                                                     |Opiskelijan suorittama kokonaiskurssimäärä 5""".stripMargin)
    }
  }
}

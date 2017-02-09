package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class LuvaSpec extends FreeSpec with Matchers with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "Lukioon valmistava koulutus (LUVA)" - {
    "Päättötodistus" in {
      todistus(MockOppijat.luva.oid, "luva") should equal("""Lukioon valmistavan koulutuksen päättötodistus
                                                     |Jyväskylän yliopisto
                                                     |Jyväskylän normaalikoulu
                                                     |Lukioonvalmistautuja, Luke 211007-442N
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

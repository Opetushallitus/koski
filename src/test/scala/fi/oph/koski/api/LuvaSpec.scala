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
                                                     |Lukioon valmistavat opinnot 7
                                                     |Äidinkieli ja kirjallisuus, Suomi toisena kielenä ja kirjallisuus 2 Hyväksytty
                                                     |Muut kielet, ruotsi 1 Hyväksytty
                                                     |Matemaattiset ja luonnontieteelliset opinnot 1 Hyväksytty
                                                     |Yhteiskuntatietous ja kulttuurintuntemus 1 Hyväksytty
                                                     |Opinto-ohjaus 1 Hyväksytty
                                                     |Tietojenkäsittely 1 Hyväksytty
                                                     |Valinnaisena suoritetut lukiokurssit 1
                                                     |A1-kieli, englanti 1 Hyväksytty
                                                     |Opiskelijan suorittama kokonaiskurssimäärä 8""".stripMargin)
    }
  }
}

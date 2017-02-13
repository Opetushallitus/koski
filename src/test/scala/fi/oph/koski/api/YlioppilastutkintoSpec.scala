package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema.{YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus}
import org.scalatest.{FreeSpec, Matchers}

class YlioppilastutkintoSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsYlioppilastutkinto with OpintosuoritusoteTestMethods with SearchTestMethods with TodistusTestMethods with LocalJettyHttpSpecification {
  "Ylioppilastutkinnot" - {
    "Lisättäessä/päivitettäessä" - {
      "palautetaan HTTP 501" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    "Haettaessa henkilötunnuksella" - {
      "Jos henkilöä ei löydy henkilöpalvelusta" - {
        "Haetaan YTR:stä ja luodaan henkilö" in {
          searchForHenkilötiedot("250493-602S").map(_.kokonimi) should equal(List("Christian Aalto"))
        }
        "Seuraavalla haulla käytetään aiemmin luotua henkilöä" in {
          searchForHenkilötiedot("250493-602S").map(_.oid) should equal(searchForHenkilötiedot("250493-602S").map(_.oid))
        }
      }
    }

    "Haettaessa opintotietoja" - {
      "Konvertoidaan YTR-järjestelmän tiedot Koski-järjestelmän opiskeluoikeudeksi" in {
        val oikeudet = getOpiskeluoikeudet(MockOppijat.ylioppilas.oid)
        oikeudet.length should equal(1)

        oikeudet(0).tyyppi.koodiarvo should equal("ylioppilastutkinto")
        oikeudet(0).suoritukset.length should equal(1)
        val tutkintoSuoritus: YlioppilastutkinnonSuoritus = oikeudet(0).asInstanceOf[YlioppilastutkinnonOpiskeluoikeus].suoritukset(0)
        tutkintoSuoritus.tila.koodiarvo should equal("VALMIS")
        tutkintoSuoritus.osasuoritusLista.length should equal(5)
        tutkintoSuoritus.osasuoritusLista.foreach { koeSuoritus =>
          koeSuoritus.tila.koodiarvo should equal("VALMIS")
        }
      }
    }

    "Todistus" - {
      "Näytetään" in {
        todistus(MockOppijat.ylioppilas.oid, "ylioppilastutkinto") should equal("""Ylioppilastutkintotodistus
                                                                                  |
                                                                                  |Helsingin medialukio
                                                                                  |Ylioppilas, Ynjevi 010696-971K
                                                                                  |
                                                                                  |Maantiede Magna cum laude approbatur
                                                                                  |Englanti, pitkä Cum laude approbatur
                                                                                  |Äidinkieli, suomi Lubenter approbatur
                                                                                  |Matematiikka, lyhyt Laudatur
                                                                                  |Ruotsi, keskipitkä Cum laude approbatur""".stripMargin)
      }
    }
  }
}

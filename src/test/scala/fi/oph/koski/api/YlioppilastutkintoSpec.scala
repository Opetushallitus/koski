package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema.{YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus}
import org.scalatest.{FunSpec, Matchers}

class YlioppilastutkintoSpec extends FunSpec with Matchers with OpiskeluoikeusTestMethodsYlioppilastutkinto with OpintosuoritusoteTestMethods with SearchTestMethods with TodistusTestMethods {
  describe("Ylioppilastutkinnot") {
    describe("Lisättäessä/päivitettäessä") {
      it("palautetaan HTTP 501") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    describe("Haettaessa henkilötunnuksella") {
      describe("Jos henkilöä ei löydy henkilöpalvelusta") {
        it("Haetaan YTR:stä ja luodaan henkilö") {
          searchForHenkilötiedot("250493-602S").map(_.kokonimi) should equal(List("Christian Aalto"))
        }
        it("Seuraavalla haulla käytetään aiemmin luotua henkilöä") {
          searchForHenkilötiedot("250493-602S").map(_.oid) should equal(searchForHenkilötiedot("250493-602S").map(_.oid))
        }
      }
    }

    describe("Haettaessa opintotietoja") {
      it("Konvertoidaan YTR-järjestelmän tiedot Koski-järjestelmän opiskeluoikeudeksi") {
        val oikeudet = opiskeluoikeudet(MockOppijat.ylioppilas.oid)
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

    describe("Todistus") {
      it("Näytetään") {
        todistus(MockOppijat.ylioppilas.oid, "ylioppilastutkinto") should equal("""Ylioppilastutkintotodistus
                                                                                  |
                                                                                  |Helsingin medialukio
                                                                                  |Ylioppilas, Ynjevi 010696-971K
                                                                                  |
                                                                                  |Maantiede M
                                                                                  |Englanti, pitkä C
                                                                                  |Äidinkieli, suomi B
                                                                                  |Matematiikka, lyhyt L
                                                                                  |Ruotsi, keskipitkä C""".stripMargin)
      }
    }
  }
}

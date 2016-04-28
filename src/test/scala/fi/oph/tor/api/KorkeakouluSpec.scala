package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.MockOppijat
import org.scalatest.{FunSpec, Matchers}

class KorkeakouluSpec extends FunSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu {
  describe("Korkeakoulun opiskeluoikeudet") {
    describe("Lisättäessä/päivitettäessä") {
      it("palautetaan HTTP 501") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, TorErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    describe("Haettaessa") {
      it("Konvertoidaan Virta-järjestelmän opiskeluoikeus") {
        val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.korkeakoululainen.oid)

        opiskeluoikeus.tyyppi.koodiarvo should equal("korkeakoulutus")
      }
    }
  }
}

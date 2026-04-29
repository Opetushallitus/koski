package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class JettyConfigurationSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  "URL-polut" - {
    "OID polulla toimii" in {
      val oid = KoskiSpecificMockOppijat.eero.oid
      authGet(s"api/oppija/${oid}") {
        response.status should (be(200) or be(404))
        response.status should not be 400
        response.status should not be 500
      }
    }

    "Prosenttikoodattu kauttaviiva polulla ei aiheuta virhettä" in {
      authGet("api/editor/koodit/koskiopiskeluoikeudentila%2Fvalmistunut") {
        response.status should not be 400
        response.status should not be 500
      }
    }
  }

  "Staattiset resurssit" - {
    "Resurssit tarjotaan oikein alipoluista" in {
      get("images/loader.svg") { verifyResponseStatusOk() }
      get("js/koski-main.js") { verifyResponseStatusOk() }
    }

    "Hakemistolistaus on estetty" in {
      get("js/") { verifyResponseStatusOk(403) }
    }
  }

  "Ei-ASCII query-parametrit" - {
    "Skandit query-parametrin nimissä toimivat" in {
      authGet("api/opiskeluoikeus?opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31") {
        response.status should not be 400
      }
    }

    "Skandit query-parametrin arvoissa toimivat" in {
      authGet("api/opiskeluoikeus?opiskeluoikeusAlkanutAikaisintaan=2016-01-01&opiskeluoikeusAlkanutViimeistään=2016-12-31") {
        response.status should not be 400
      }
    }
  }
}

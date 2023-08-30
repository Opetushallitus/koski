package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class StaticPagesSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  "Single page app" - {
    //TODO: kansalainen
    verifyAppAt("virkailija")
    verifyAppAt("oppija/asdf")
    verifyAppAt("uusioppija")
    verifyAppAt("asdf", 404)

    def verifyAppAt(path: String, responseCode: Int = 200) = {
      "GET " + path in {
        authGet(path) {
          verifyResponseStatusOk(responseCode)
          body should include("<title>Koski - Opintopolku.fi</title>")
        }
      }
    }
  }

  "Static resources" in {
    get("images/loader.svg") { verifyResponseStatusOk()}
    get("js/koski-main.js") { verifyResponseStatusOk()}
    get("js/koski-login.js") { verifyResponseStatusOk()}
    get("test/runner.html") { verifyResponseStatusOk()}
    get("js/codemirror/codemirror.js") { verifyResponseStatusOk()}
  }

  "No directory browsing" in {
    get("js/") { verifyResponseStatusOk(403) }
    get("css/") { verifyResponseStatusOk(403) }
    get("external_css/") { verifyResponseStatusOk(403) }
    get("js") { verifyResponseStatusOk(302) }
    get("css") { verifyResponseStatusOk(302) }
    get("external_css") { verifyResponseStatusOk(302) }
  }

  "Documentation" in {
    get("documentation") { verifyResponseStatusOk(302)}
    get("dokumentaatio") { verifyResponseStatusOk()}
  }
}

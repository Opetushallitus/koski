package fi.oph.koski.api

import org.scalatest.{FreeSpec, Matchers}

class StaticPagesSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {
  "Single page app" - {
    verifyAppAt("")
    verifyAppAt("oppija/asdf")
    verifyAppAt("uusioppija")
    verifyAppAt("asdf", 404)

    def verifyAppAt(path: String, responseCode: Int = 200) = {
      "GET " + path in {
        authGet(path) {
          verifyResponseStatusOk(responseCode)
          body.contains("<title>Koski - Opintopolku.fi</title>")
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

  "Documentation" in {
    get("documentation") { verifyResponseStatusOk(302)}
    get("documentaatio") { verifyResponseStatusOk()}
  }
}
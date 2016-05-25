package fi.oph.koski.api

import fi.oph.koski.util.Files
import org.scalatest.{FreeSpec, Matchers}

class StaticPagesSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {
  val indexHtml = Files.asString("web/static/index.html").get

  "Single page app" - {
    "GET /" in {
      get("/") {
        body should equal(indexHtml)
        verifyResponseStatus(200)
      }
    }

    "GET /oppija/*" in {
      get("oppija/asdf") {
        body should equal(indexHtml)
        verifyResponseStatus(200)
      }
    }

    "GET /uusioppija" in {
      get("uusioppija") {
        body should equal(indexHtml)
        verifyResponseStatus(200)
      }
    }

    "GET /asdf" in {
      get("asdf") {
        body should equal(indexHtml)
        verifyResponseStatus(404)
      }
    }
  }

  "Static resources" in {
    get("images/loader.gif") { verifyResponseStatus(200)}
    get("bundle.js") { verifyResponseStatus(200)}
    get("test/runner.html") { verifyResponseStatus(200)}
    get("codemirror/lib/codemirror.js") { verifyResponseStatus(200)}
  }

  "Documentation" in {
    get("documentation") { verifyResponseStatus(200)}
  }
}
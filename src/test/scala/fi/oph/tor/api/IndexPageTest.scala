package fi.oph.tor.api

import fi.oph.tor.util.Files
import org.scalatest.{Matchers, FreeSpec}

class IndexPageTest extends FreeSpec with HttpSpecification with Matchers {
  val indexHtml = Files.asString("web/static/index.html").get

  "GET /" in {
    get("/") {
      verifyResponseStatus(200)
      body should equal(indexHtml)
    }
  }

  "GET /asdf" in {
    get("/asdf") {
      verifyResponseStatus(404)
      body should equal(indexHtml)
    }
  }
}
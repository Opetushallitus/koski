package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class CachingSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {
  "API caching is disabled" - {
    "/oppija" in { verifyNoCache("api/oppija/" + MockOppijat.eero.oid)}
  }

  def verifyNoCache(path: String): Unit = {
    authGet(path) {
      verifyResponseStatus(200)
      response.getHeader("Cache-Control") should equal("no-store, no-cache, must-revalidate")
    }
  }
}

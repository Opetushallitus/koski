package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CachingSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  "API caching is disabled" - {
    "/oppija" in { verifyNoCache("api/oppija/" + KoskiSpecificMockOppijat.eero.oid)}
  }

  def verifyNoCache(path: String): Unit = {
    authGet(path) {
      verifyResponseStatusOk()
      response.getHeader("Cache-Control") should equal("no-store, no-cache, must-revalidate")
    }
  }
}

package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "/api/opiskeluoikeus/:oid" - {
    "GET" - {
      "with valid oid" in {
        val oid = lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get
        AuditLogTester.clearMessages
        get("api/opiskeluoikeus/" + oid, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with unknown oid" in {
        get("api/opiskeluoikeus/1.2.246.562.15.63039018849", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
      "with invalid oid" in {
        get("api/opiskeluoikeus/0", headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid("Virheellinen oid: 0. Esimerkki oikeasta muodosta: 1.2.246.562.15.00000000001."))
        }
      }
    }
  }
}

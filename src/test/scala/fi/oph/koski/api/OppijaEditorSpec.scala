package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.common.log.AuditLogTester
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class OppijaEditorSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = resetFixtures

  "GET /api/editor/:oid" - {
    "with valid oid" in {
      AuditLogTester.clearMessages
      get("api/editor/" + MockOppijat.eero.oid, headers = authHeaders()) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with version number" in {
      val opiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get
      AuditLogTester.clearMessages
      get("api/editor/" + MockOppijat.eero.oid, params = List("opiskeluoikeus" -> opiskeluoikeusOid, "versionumero" -> "1"), headers = authHeaders()) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with invalid oid" in {
      get("api/editor/blerg", headers = authHeaders()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: blerg. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
      }
    }
    "with unknown oid" in {
      get("api/editor/1.2.246.562.24.90000000001", headers = authHeaders()) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.90000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "with Virta error" in {
      get("api/editor/" + MockOppijat.virtaEiVastaa.oid, headers = authHeaders()) {
        verifyResponseStatusOk()
        body should include("\"unavailable.virta\"")
      }
    }
  }

  "GET /api/omattiedot/editor" - {
    "with virkailija login -> forbidden" in {
      AuditLogTester.clearMessages
      get("api/omattiedot/editor", headers = authHeaders(user = MockUsers.omattiedot)) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
      }
    }
    "with kansalainen login -> logs KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN" in {
      AuditLogTester.clearMessages
      get("api/omattiedot/editor", headers = kansalainenLoginHeaders("190751-739W")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with Virta error" in {
      get("api/omattiedot/editor", headers = kansalainenLoginHeaders(MockOppijat.virtaEiVastaa.hetu.get)) {
        verifyResponseStatusOk()
        body should include("\"unavailable.virta\"")
      }
    }
  }
}


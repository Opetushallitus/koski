package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.Tag

class ValpasRootApiServletSpec extends ValpasTestMethods {
  override def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  "Oppijan lataaminen tuottaa auditlogin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = "1.2.246.562.24.00000000001"
    AuditLogTester.clearMessages
    authGet(s"/valpas/api/oppija/$oppijaOid") {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> "VALPAS_OPPIJA_KATSOMINEN",
        "target" -> Map("OPPIJA_OID" -> oppijaOid)))
    }
  }
}

object ValpasBackendTag extends Tag("valpasback")

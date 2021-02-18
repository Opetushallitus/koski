package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.Tag

class ValpasRootApiServletSpec extends ValpasTestMethods {
  override def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  "Oppijan lataaminen tuottaa auditlogin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    AuditLogTester.clearMessages
    authGet(s"/valpas/api/oppija/$oppijaOid") {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> "VALPAS_OPPIJA_KATSOMINEN",
        "target" -> Map("OPPIJA_OID" -> oppijaOid)))
    }
  }

  "Ei-oppivelvollisen oppijan tietojen lataaminen ei onnistu" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004
    authGet(s"/valpas/api/oppija/$oppijaOid") {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
    }
  }
}

object ValpasBackendTag extends Tag("valpasback")

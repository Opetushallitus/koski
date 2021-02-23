package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.Tag

class ValpasRootApiServletSpec extends ValpasTestBase with ValpasHttpTestBase {
  override def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  "Oppijan lataaminen tuottaa auditlogin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    AuditLogTester.clearMessages
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> "VALPAS_OPPIJA_KATSOMINEN",
        "target" -> Map("OPPIJA_OID" -> oppijaOid)))
    }
  }

  "Ei-oppivelvollisen oppijan tietojen lataaminen ei onnistu" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
    }
  }

  "Oppija, johon ei ole oikeuksia, ja jota ei ole olemassa tuottavat saman vastaukset" taggedAs(ValpasBackendTag) in {
    val jklOppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021
    authGet(getOppijaUrl(jklOppija.oid), ValpasMockUsers.valpasHelsinki) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      val firstResponse = response

      authGet(getOppijaUrl("1.2.3.4.5.6.7"), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
        response.body should equal (firstResponse.body)
        response.headers should equal (firstResponse.headers)
      }
    }
  }

  def getOppijaUrl(oppijaOid: String) = s"/valpas/api/oppija/$oppijaOid"
}

object ValpasBackendTag extends Tag("valpasback")

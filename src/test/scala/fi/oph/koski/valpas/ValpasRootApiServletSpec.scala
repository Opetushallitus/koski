package fi.oph.koski.valpas

import fi.oph.koski.log.{AuditLogTester, KoskiMessageField}
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.log.ValpasOperation
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.Tag

class ValpasRootApiServletSpec extends ValpasHttpTestBase {
  override def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  "Oppijan lataaminen tuottaa auditlogin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    AuditLogTester.clearMessages
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KATSOMINEN.toString,
        "target" -> Map(KoskiMessageField.oppijaHenkiloOid.toString -> oppijaOid)))
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
        withoutVariatingEntries(response.headers) should equal (withoutVariatingEntries(firstResponse.headers))
      }
    }
  }

  def getOppijaUrl(oppijaOid: String) = s"/valpas/api/oppija/$oppijaOid"

  def withoutVariatingEntries[T](headers: Map[String, T]) =
    headers.filterKeys(_ != "Date")
}

object ValpasBackendTag extends Tag("valpasback")

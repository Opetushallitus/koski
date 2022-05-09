package fi.oph.koski.valpas

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{BeforeAndAfterEach, Tag}

class ValpasRootApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "Oppijan lataaminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KATSOMINEN.toString,
        "target" -> Map(ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> oppijaOid)))
    }
  }

  "Oppilaitoksen oppijalistan hakeminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    authGet(getOppijaListUrl(oppilaitosOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN.toString,
        "target" -> Map(ValpasAuditLogMessageField.juuriOrganisaatio.toString -> oppilaitosOid)))
    }
  }

  "Oppilaitoksen oppijalistan hakeminen hakutiedoilla tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    val oppijaOids = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    post(
      getOppijaListHakutiedoillaUrl(oppilaitosOid),
      JsonSerializer.writeWithRoot(Oppijalista(oppijaOids)),
      headers = authHeaders(defaultUser) ++ jsonContent,
    ) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN_HAKUTIEDOILLA.toString,
        "target" -> Map(
          ValpasAuditLogMessageField.juuriOrganisaatio.toString -> oppilaitosOid,
          ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> oppijaOids.mkString(","),
          ValpasAuditLogMessageField.sivu.toString -> "1",
          ValpasAuditLogMessageField.sivuLukumäärä.toString -> "1",
        )))
    }
  }

  "Ei-oppivelvollisen oppijan tietojen lataaminen ei onnistu" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      AuditLogTester.verifyNoAuditLogMessages()
    }
  }

  "Oppija, johon ei ole oikeuksia, ja jota ei ole olemassa tuottavat saman vastaukset" taggedAs(ValpasBackendTag) in {
    val jklOppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021
    authGet(getOppijaUrl(jklOppija.oid), ValpasMockUsers.valpasHelsinkiPeruskoulu) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      val firstResponse = response

      authGet(getOppijaUrl("1.2.3.4.5.6.7"), ValpasMockUsers.valpasHelsinkiPeruskoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
        response.body should equal (firstResponse.body)
        withoutVariatingEntries(response.headers) should equal (withoutVariatingEntries(firstResponse.headers))
        AuditLogTester.verifyNoAuditLogMessages()
      }
    }
  }

  "Hetu ei päädy lokiin - kunta" in {
    testHetunMaskausAccessLogissa(getHenkilöhakuKuntaUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
  }

  "Hetu ei päädy lokiin - maksuttomuus" in {
    testHetunMaskausAccessLogissa(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
  }

  "Hetu ei päädy lokiin - suorittaminen" in {
    testHetunMaskausAccessLogissa(getHenkilöhakuSuorittaminenUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
  }

  private def testHetunMaskausAccessLogissa(url: String) = {
    AccessLogTester.clearMessages
    val maskedHetu = "******-****"
    authGet(url, ValpasMockUsers.valpasMonta) {
      verifyResponseStatusOk()
      Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
      AccessLogTester.getLogMessages.lastOption.get should include(maskedHetu)
    }
  }

  private def getHenkilöhakuKuntaUrl(hetu: String) = s"/valpas/api/henkilohaku/kunta/$hetu"
  private def getHenkilöhakuMaksuttomuusUrl(hetu: String) = s"/valpas/api/henkilohaku/maksuttomuus/$hetu"
  private def getHenkilöhakuSuorittaminenUrl(hetu: String) = s"/valpas/api/henkilohaku/suorittaminen/$hetu"

  def getOppijaUrl(oppijaOid: String) = s"/valpas/api/oppija/$oppijaOid"

  def getOppijaListUrl(organisaatioOid: String) = s"/valpas/api/oppijat/$organisaatioOid"
  def getOppijaListHakutiedoillaUrl(organisaatioOid: String) = s"/valpas/api/oppijat/$organisaatioOid/hakutiedot"

  def withoutVariatingEntries[T](headers: Map[String, T]) =
    headers.filterKeys(_ != "Date")
}

object ValpasBackendTag extends Tag("valpasback")


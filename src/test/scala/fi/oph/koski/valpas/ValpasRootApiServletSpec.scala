package fi.oph.koski.valpas

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AuditLogTester, KoskiMessageField}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.valpas.log.ValpasOperation
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasrepository.ValpasKunta
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{BeforeAndAfterEach, Tag}

class ValpasRootApiServletSpec extends ValpasHttpTestBase with BeforeAndAfterEach {
  override def defaultUser = ValpasMockUsers.valpasJklNormaalikoulu

  override def beforeEach() {
    AuditLogTester.clearMessages
  }

  "Oppijan lataaminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KATSOMINEN.toString,
        "target" -> Map(KoskiMessageField.oppijaHenkiloOid.toString -> oppijaOid)))
    }
  }

  "Oppilaitoksen oppijalistan hakeminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    authGet(getOppijaListUrl(oppilaitosOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN.toString,
        "target" -> Map(KoskiMessageField.juuriOrganisaatio.toString -> oppilaitosOid)))
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
    authGet(getOppijaUrl(jklOppija.oid), ValpasMockUsers.valpasHelsinki) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      val firstResponse = response

      authGet(getOppijaUrl("1.2.3.4.5.6.7"), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
        response.body should equal (firstResponse.body)
        withoutVariatingEntries(response.headers) should equal (withoutVariatingEntries(firstResponse.headers))
        AuditLogTester.verifyNoAuditLogMessages()
      }
    }
  }

  "Palauttaa kunnat" taggedAs(ValpasBackendTag) in {
    authGet(s"/valpas/api/organisaatiot/kunnat", ValpasMockUsers.valpasJklNormaalikoulu) {
      verifyResponseStatusOk()
      val actualKunnat = JsonSerializer.parse[List[ValpasKunta]](response.body)
        .map(vk => (vk.nimi.get.get("fi"), vk.kotipaikka.get.koodiarvo, vk.kotipaikka.get.getNimi.get.get("fi")))
        .sortBy(_._1)

      val expectedKunnat: List[Tuple3[String, String, String]] = List(
        ("Helsingin kaupunki", "091", "Helsinki"),
        ("Pyhtään kunta", "624", "Pyhtää")
      )

      actualKunnat should equal (expectedKunnat)
    }
  }

  def getOppijaUrl(oppijaOid: String) = s"/valpas/api/oppija/$oppijaOid"

  def getOppijaListUrl(organisaatioOid: String) = s"/valpas/api/oppijat/$organisaatioOid"

  def withoutVariatingEntries[T](headers: Map[String, T]) =
    headers.filterKeys(_ != "Date")
}

object ValpasBackendTag extends Tag("valpasback")

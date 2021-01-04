package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.scalatest.{FreeSpec, Matchers}

class AuditLogServletSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "AuditLogServlet" - {
    "Katsoja voi kuulua useaan organisaatioon, logit ryhmitellään katsojan organisaatioiden perusteella" in {
      auditlogs(MockOppijat.amis).map(_.organizations.map(_.oid)) should contain theSameElementsAs(List(
        List(MockOrganisaatiot.helsinginKaupunki, MockOrganisaatiot.stadinAmmattiopisto),
        List(MockOrganisaatiot.helsinginKaupunki)
      ))
    }
    "Ei näytetä auditlogeja joissa operaationa on ollut opiskeluoikeuden päivitys/lisäys" in {
      auditlogs(MockOppijat.amis).map(_.organizations.map(_.oid)) shouldNot contain theSameElementsAs(List(
        List(MockOrganisaatiot.ressunLukio)
      ))
    }
    "Ei näytetä kansalaisen omia katseluja" in {
      auditlogs(MockOppijat.ammattilainen) shouldBe empty
    }
    "Huollettavalle ei näytetä huoltajan katseluja" in {
      auditlogs(MockOppijat.lukiolainen) shouldBe empty
    }
    "Organisaation oidia, jota ei löydy organisaatiopalvelusta, ei osata käsitellä" in {
      get("api/auditlogs", headers = kansalainenLoginHeaders(MockOppijat.virtaEiVastaa.hetu.get)) {
        verifyResponseStatus(500, KoskiErrorCategory.internalError())
      }
    }
    "Rajapinta vaatii kirjautumisen" in {
      get("api/auditlogs") {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
      }
    }
  }

  private def auditlogs(oppija: LaajatOppijaHenkilöTiedot) = {
    get("api/auditlogs", headers = kansalainenLoginHeaders(oppija.hetu.get)) {
      verifyResponseStatusOk()
      JsonSerializer.parse[List[OrganisaationAuditLogit]](body)
    }
  }
}

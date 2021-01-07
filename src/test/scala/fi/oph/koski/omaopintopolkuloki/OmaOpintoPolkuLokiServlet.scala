package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import org.scalatest.{FreeSpec, Matchers}

class OmaOpintoPolkuLokiServletSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "AuditLogien näyttäminen kansalaiselle" - {
    "Katsoja voi kuulua useaan organisaatioon, logit ryhmitellään katsojan organisaatioiden perusteella" in {
      auditlogs(MockOppijat.amis).map(_.organizations.map(_.oid)) should contain theSameElementsAs(List(
        List(MockOrganisaatiot.helsinginKaupunki, MockOrganisaatiot.stadinAmmattiopisto),
        List(MockOrganisaatiot.helsinginKaupunki)
      ))
    }
    "Jos katselijan organisaatio on Opetushallitus" in {
      auditlogs(MockOppijat.aikuisOpiskelija).map(_.organizations.map(_.oid)) should contain theSameElementsAs(List(
        List(Opetushallitus.organisaatioOid)
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
      get("api/omaopintopolkuloki/auditlogs", headers = kansalainenLoginHeaders(MockOppijat.virtaEiVastaa.hetu.get)) {
        verifyResponseStatus(500, KoskiErrorCategory.internalError())
      }
    }
    "Rajapinta vaatii kirjautumisen" in {
      get("api/omaopintopolkuloki/auditlogs") {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
      }
    }
  }
  "Henkilötietojen palauttaminen käyttöliittymään" - {
    "Palauttaa henkilötiedot" in {
      val oppija = MockOppijat.ammattilainen
      get("api/omaopintopolkuloki/whoami", headers = kansalainenLoginHeaders(oppija.hetu.get)) {
        verifyResponseStatusOk()
        JsonSerializer.parse[OmaOpintopolkuLokiHenkiloTiedot](body) should equal(
          OmaOpintopolkuLokiHenkiloTiedot(
            hetu = oppija.hetu,
            etunimi = oppija.etunimet,
            kutsumanimi = oppija.kutsumanimi,
            sukunimi = oppija.sukunimi
          )
        )
      }
    }
  }

  private def auditlogs(oppija: LaajatOppijaHenkilöTiedot) = {
    get("api/omaopintopolkuloki/auditlogs", headers = kansalainenLoginHeaders(oppija.hetu.get)) {
      verifyResponseStatusOk()
      JsonSerializer.parse[List[OrganisaationAuditLogit]](body)
    }
  }
}

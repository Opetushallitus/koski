package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OmaOpintoPolkuLokiServletSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {
  "AuditLogien näyttäminen kansalaiselle" - {
    "Katsoja voi kuulua useaan organisaatioon, logit ryhmitellään katsojan organisaatioiden perusteella" in {
      auditlogs(KoskiSpecificMockOppijat.amis).map(_.organizations.map(_.oid)) should contain theSameElementsAs(List(
        List(MockOrganisaatiot.helsinginKaupunki, MockOrganisaatiot.stadinAmmattiopisto),
        List(MockOrganisaatiot.helsinginKaupunki)
      ))
    }
    "Jos katselijan organisaatio on Opetushallitus" in {
      auditlogs(KoskiSpecificMockOppijat.aikuisOpiskelija).map(_.organizations.map(_.oid)) should contain theSameElementsAs(List(
        List(Opetushallitus.organisaatioOid)
      ))
    }
    "Ei näytetä auditlogeja joissa operaationa on ollut opiskeluoikeuden päivitys/lisäys" in {
      auditlogs(KoskiSpecificMockOppijat.amis).map(_.organizations.map(_.oid)) shouldNot contain theSameElementsAs(List(
        List(MockOrganisaatiot.ressunLukio)
      ))
    }
    "Ei näytetä kansalaisen omia katseluja" in {
      auditlogs(KoskiSpecificMockOppijat.ammattilainen) shouldBe empty
    }
    "Huollettavalle ei näytetä huoltajan katseluja" in {
      auditlogs(KoskiSpecificMockOppijat.lukiolainen) shouldBe empty
    }
    "Organisaation oidia, jota ei löydy organisaatiopalvelusta, ei osata käsitellä" in {
      auditlogsRequest(KoskiSpecificMockOppijat.virtaEiVastaa) {
        verifyResponseStatus(500, KoskiErrorCategory.internalError())
      }
    }
    "Rajapinta vaatii kirjautumisen" in {
      post("api/omaopintopolkuloki/auditlogs",
        body = JsonSerializer.writeWithRoot(Map("hetu" -> "")),jsonContent
      ) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
      }
    }
  }
  "Henkilötietojen palauttaminen käyttöliittymään" - {
    "Palauttaa henkilötiedot" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen
      get("api/omaopintopolkuloki/whoami", headers = kansalainenLoginHeaders(oppija.hetu.get)) {
        verifyResponseStatusOk()
        JsonSerializer.parse[OmaOpintopolkuLokiHenkiloTiedot](body) should equal(
          OmaOpintopolkuLokiHenkiloTiedot(
            hetu = oppija.hetu,
            etunimet = oppija.etunimet,
            kutsumanimi = oppija.kutsumanimi,
            sukunimi = oppija.sukunimi,
            huollettavat = None
          )
        )
      }
    }
  }

  private def auditlogs(oppija: LaajatOppijaHenkilöTiedot) = {
    auditlogsRequest(oppija) {
      verifyResponseStatusOk()
      JsonSerializer.parse[List[OrganisaationAuditLogit]](body)
    }
  }

  private def auditlogsRequest[T](oppija: LaajatOppijaHenkilöTiedot)(f: => T): T = {
    post("api/omaopintopolkuloki/auditlogs",
      body = JsonSerializer.writeWithRoot(Map("hetu" -> oppija.hetu)),
      headers = kansalainenLoginHeaders(oppija.hetu.get) ++ jsonContent
    )(f)
  }
}

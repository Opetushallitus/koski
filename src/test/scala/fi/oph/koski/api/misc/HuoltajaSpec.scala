package fi.oph.koski.api.misc

import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.freespec.AnyFreeSpec

import java.nio.charset.StandardCharsets

class HuoltajaSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus with DirtiesFixtures {
  private implicit val formats: DefaultFormats = DefaultFormats

  "Huollettavan tietojen katselu" - {
    "Tarkistaa, että huollettavat tulevat login-datan mukana" in {
      val loginHeaders = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor", headers = loginHeaders) {
        verifyResponseStatusOk()
        val huollettavat = huollettavienTiedot
        val etunimet = huollettavat.map(huollettava => {
          huollettava("etunimet")
        }).sorted
        etunimet should equal (List("Essi", "Olli", "Tero", "Ynjevi"))
      }
    }

    "Kysytään huollettavan opintotiedot" in {
      val loginHeaders = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + KoskiSpecificMockOppijat.eskari.oid, headers = loginHeaders) {
        verifyResponseStatusOk()
        val nimet = nimitiedot
        nimet("etunimet") should equal("Essi")
      }
    }

    "Huollettavan tiedoissa näytetään samat erityiset henkilötiedot kuin huollettavalle itselleen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, KoskiSpecificMockOppijat.eskari) {
        verifyResponseStatusOk()
      }
      val loginHeaders = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + KoskiSpecificMockOppijat.eskari.oid, headers = loginHeaders) {
        verifyResponseStatusOk()
        val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)
        bodyString should include("yksilöllistettyOppimäärä")
        bodyString should not include("jääLuokalle")
      }
    }

    "Muiden kuin huollettavien tietojen katselu on estetty" in {
      val loginHeaders = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + KoskiSpecificMockOppijat.amis.oid, headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "Aiheuttaa auditlog-merkinnän" in {
      val loginHeaders = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid, headers = loginHeaders) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN"))
        }
    }
  }

  def huollettavienTiedot =
    JsonMethods.parse(body)
      .filter(json => (json \ "key").extractOpt[String].contains("huollettavat"))
      .map(json => json \ "model" \ "value")
      .flatMap(json => json.extract[List[JObject]])
      .map(json => json.filter(json => (json \ "key").extractOpt[String].exists(k => k == "etunimet" || k == "sukunimi" )))
      .map(json => json
        .map { huollettavaJson =>
          (huollettavaJson \ "key").extract[String] -> (huollettavaJson \ "model" \ "value" \ "data").extract[String]
        }.toMap)

  def nimitiedot =
    JsonMethods.parse(body)
      .filter(json => (json \ "key").extractOpt[String].contains("henkilö"))
      .flatMap(json => (json \ "model" \ "value" \ "properties").extract[List[JObject]])
      .filter { henkilöJson =>
        (henkilöJson \ "key").extractOpt[String].exists(k => k == "etunimet" || k == "kutsumanimi" || k == "sukunimi")
      }
      .map { nimiJson =>
        (nimiJson \ "key").extract[String] -> (nimiJson \ "model" \ "value" \ "data").extract[String]
      }.toMap
}

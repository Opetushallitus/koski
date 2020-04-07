package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.log.AuditLogTester
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.FreeSpec

class HuoltajaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  implicit val formats = DefaultFormats

  "Huollettavan tietojen katselu" - {
    "Tarkistaa, että huollettavat tulevat login-datan mukana" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor", headers = loginHeaders) {
        verifyResponseStatusOk()
        val huollettavat = huollettavienTiedot
        val etunimet = huollettavat.map(huollettava => {
          huollettava("etunimet")
        }).sorted
        etunimet should equal (List("Essi", "Olli", "Ynjevi"))
      }
    }

    "Kysytään huollettavan opintotiedot" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + MockOppijat.eskari.oid, headers = loginHeaders) {
        verifyResponseStatusOk()
        val nimet = nimitiedot
        nimet("etunimet") should equal("Essi")
      }
    }

    "Aiheuttaa auditlog-merkinnän" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.faija.hetu.get)

      get(s"api/omattiedot/editor/" + MockOppijat.ylioppilasLukiolainen.oid, headers = loginHeaders) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN"))
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

  def päätasonSuoritukset = JsonMethods.parse(body)
    .filter(json => (json \ "key").extractOpt[String].contains("suoritukset") && (json \ "model" \ "value").toOption.isDefined)
    .map(json => json \ "model" \ "value")
    .flatMap(json => json.extract[List[JObject]])
}

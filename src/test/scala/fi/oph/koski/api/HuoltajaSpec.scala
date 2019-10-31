package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.ytr.ValtuutusTestMethods
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.FreeSpec

class HuoltajaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus with ValtuutusTestMethods {
  implicit val formats = DefaultFormats

  "Huollettavan tietojen katselu" - {
    "aiheutttaa auditlog merkinnän" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.aikuisOpiskelija.hetu.get)
      get("huoltaja/valitse", headers = loginHeaders) {
        get(s"api/omattiedot/editor/$valtuutusCode", headers = loginHeaders) {
          verifyResponseStatusOk()
          päätasonSuoritukset.length should equal(2)
          (päätasonSuoritukset.head \ "value" \ "classes").extract[List[String]] should contain("ylioppilastutkinnonsuoritus")
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
    }

    "oidittaa huollettavan jos ei löydy oppijanumerorekisteristä" in {
      val loginHeaders = kansalainenLoginHeaders(MockOppijat.eerola.hetu.get)
      get("huoltaja/valitse", headers = loginHeaders) {
        get(s"api/omattiedot/editor/$valtuutusCode", headers = loginHeaders) {
          verifyResponseStatusOk()
          val nimet = nimitiedot
          nimet("etunimet") should equal("Erkki Einari")
          nimet("kutsumanimi") should equal("Erkki")
          nimet("sukunimi") should equal("Eioppijanumerorekisterissä")
        }
      }
    }
  }

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

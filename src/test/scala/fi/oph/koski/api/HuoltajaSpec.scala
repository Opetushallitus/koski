package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.log.AuditLogTester
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.FreeSpec

class HuoltajaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
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
  }

  private def valtuutusCode = {
    response.headers("Location").head.split("=").last
  }

  def päätasonSuoritukset = JsonMethods.parse(body)
    .filter(json => (json \ "key").extractOpt[String].contains("suoritukset") && (json \ "model" \ "value").toOption.isDefined)
    .map(json => json \ "model" \ "value")
    .flatMap(json => json.extract[List[JObject]])
}

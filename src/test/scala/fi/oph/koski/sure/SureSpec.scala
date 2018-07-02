package fi.oph.koski.sure

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JsonAST.{JArray, JBool}
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class SureSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers {

  import fi.oph.koski.schema.KoskiSchema.deserializationContext

  "Sure-rajapinnat" - {
    "/api/sure/oids" - {
      "Palauttaa pyydetyt OIDit" in {
        val henkilöOids = Set(MockOppijat.amis.oid, MockOppijat.lukiolainen.oid)
        postOids(henkilöOids) {
          verifyResponseStatusOk()
          val oppijat = SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
          oppijat.map(_.henkilö).toSet should equal(henkilöOids.map(OidHenkilö))
        }
      }
      "Palauttaa tyhjän listan jos OIDia ei löydy" in {
        postOids(Seq("1.2.246.562.24.90000000001")) {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa virheen jos OID on virheellinen" in {
        postOids(Seq("foobar")) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: foobar. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
      "Palauttaa virheen jos liian monta OIDia" in {
        val oids = List.fill(2000)("1.2.246.562.24.90000000001")
        postOids(oids) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Liian monta oidia, enintään 1000 sallittu."))
        }
      }
      "Tuottaa oikean audit log viestin" in {
        val oids = Seq("1.2.246.562.24.90000000001", "1.2.246.562.24.90000000002", "1.2.246.562.24.90000000003", "1.2.246.562.24.90000000004")
        postOids(oids) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("hakuEhto" -> "oids=1.2.246.562.24.90000000001,1.2.246.562.24.90000000002,...(4)")))
        }
      }
      "Luottamuksellinen data" - {
        "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN-rooli" in {
          postOids(Seq(MockOppijat.eero.oid), user = stadinAmmattiopistoKatselija) {
            verifyResponseStatusOk()
            body should include("vankilaopetuksessa")
          }
        }
        "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN-rooli" in {
          postOids(Seq(MockOppijat.eero.oid), user = stadinVastuukäyttäjä) {
            verifyResponseStatusOk()
            body should not include("vankilaopetuksessa")
          }
        }
      }
      "Lasketut kentät palautetaan" in {
        postOids(Seq(MockOppijat.valma.oid), user = stadinAmmattiopistoKatselija) {
          verifyResponseStatusOk()
          val parsedJson = JsonMethods.parse(body)
          val ensimmäisenOsasuorituksetArviointi = ((((parsedJson \ "opiskeluoikeudet") (0) \ "suoritukset") (0) \ "osasuoritukset") (0) \ "arviointi") (0)
          (ensimmäisenOsasuorituksetArviointi \ "hyväksytty") should equal(JArray(List(JBool(true))))
        }
      }
    }
  }

  private def postOids[A](oids: Iterable[String], user: UserWithPassword = defaultUser)(f: => A) =
    post("api/sure/oids", "[" + oids.map("\"" + _ + "\"").mkString(",") + "]", authHeaders(user) ++ jsonContent)(f)
}


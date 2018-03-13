package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillinenOpiskeluoikeusjakso}
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with HttpTester with OpiskeluoikeusTestMethodsAmmatillinen {
  "/api/opiskeluoikeus/:oid" - {
    "GET" - {
      "with valid oid" in {
        resetFixtures
        val oid = lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get
        AuditLogTester.clearMessages
        get("api/opiskeluoikeus/" + oid, headers = authHeaders()) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with unknown oid" in {
        get("api/opiskeluoikeus/1.2.246.562.15.63039018849", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
      "with invalid oid" in {
        get("api/opiskeluoikeus/0", headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid("Virheellinen oid: 0. Esimerkki oikeasta muodosta: 1.2.246.562.15.00000000001."))
        }
      }
      "with mitätöity oid" in {
        resetFixtures
        val mitätöity = mitätöiOpiskeluoikeus(createOpiskeluoikeus(MockOppijat.eero, defaultOpiskeluoikeus))
        List(defaultUser, MockUsers.paakayttaja).foreach { user =>
          get("api/opiskeluoikeus/" + mitätöity.oid.get, headers = authHeaders(user = user)) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
          }
        }
      }
    }

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN-rooli" in {
        resetFixtures
        val oid = lastOpiskeluoikeusByHetu(MockOppijat.eero).oid.get
        authGet("api/opiskeluoikeus/" + oid, stadinAmmattiopistoKatselija) {
          verifyResponseStatusOk()
          vankilaopetusValue should be(true)
        }
      }

      "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN-rooli" in {
        val oid = lastOpiskeluoikeusByHetu(MockOppijat.eero).oid.get
        authGet("api/opiskeluoikeus/" + oid, stadinVastuukäyttäjä) {
          verifyResponseStatusOk()
          vankilaopetusValue should be(false)
        }
      }
    }
  }

  private def mitätöiOpiskeluoikeus(oo: AmmatillinenOpiskeluoikeus) = {
    delete(s"api/opiskeluoikeus/${oo.oid.get}", headers = authHeaders())(verifyResponseStatusOk())
    oo
  }

  private def vankilaopetusValue = readOpiskeluoikeus match {
    case a: AmmatillinenOpiskeluoikeus => a.lisätiedot.exists(_.vankilaopetuksessa.isDefined)
  }
}

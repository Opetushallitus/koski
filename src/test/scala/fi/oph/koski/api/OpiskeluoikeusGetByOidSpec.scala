package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with HttpTester {
  "/api/opiskeluoikeus/:oid" - {
    "GET" - {
      "with valid oid" in {
        val oid = lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get
        AuditLogTester.clearMessages
        get("api/opiskeluoikeus/" + oid, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
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
    }

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN rooli" in {
        val oid = lastOpiskeluoikeusByHetu(MockOppijat.eero).oid.get
        authGet("api/opiskeluoikeus/" + oid, stadinAmmattiopistoKatselija) {
          verifyResponseStatus(200)
          vankilaopetusValue should be(true)
        }
      }

      "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN rooli" in {
        val oid = lastOpiskeluoikeusByHetu(MockOppijat.eero).oid.get
        authGet("api/opiskeluoikeus/" + oid, stadinVastuukäyttäjä) {
          verifyResponseStatus(200)
          vankilaopetusValue should be(false)
        }
      }
    }
  }


  private def vankilaopetusValue = readOpiskeluoikeus match {
    case a: AmmatillinenOpiskeluoikeus => a.lisätiedot.exists(_.vankilaopetuksessa)
  }
}

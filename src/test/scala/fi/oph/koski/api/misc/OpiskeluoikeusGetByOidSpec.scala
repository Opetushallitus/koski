package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeusGetByOidSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods with OpiskeluoikeusTestMethodsAmmatillinen {
  "/api/opiskeluoikeus/:oid" - {
    "GET" - {
      "with valid oid" in {
        val oid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid).oid.get
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
        val mitätöity = mitätöiOpiskeluoikeus(createOpiskeluoikeus(KoskiSpecificMockOppijat.eero, defaultOpiskeluoikeus))
        List(defaultUser, MockUsers.paakayttaja).foreach { user =>
          get("api/opiskeluoikeus/" + mitätöity.oid.get, headers = authHeaders(user = user)) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
          }
        }
        resetFixtures() // Siivoa muutokset
      }
    }

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        val oid = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.eero.toHenkilötiedotJaOid).oid.get
        authGet("api/opiskeluoikeus/" + oid, stadinAmmattiopistoKatselija) {
          verifyResponseStatusOk()
          vankilaopetusValue should be(true)
        }
      }

      "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        val oid = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.eero.toHenkilötiedotJaOid).oid.get
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

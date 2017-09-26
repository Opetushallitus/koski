package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillinenOpiskeluoikeusjakso}
import org.scalatest.{FreeSpec, Matchers}

import scala.reflect.runtime.universe.TypeTag

class OpiskeluoikeusGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with HttpTester with PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  "/api/opiskeluoikeus/:oid" - {
    "GET" - {
      "with valid oid" in {
        resetFixtures
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
      "with mitätöity oid" in {
        val mitätöity = defaultOpiskeluoikeus.copy(tila = defaultOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot =
          defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
        ))
        putOpiskeluoikeus(mitätöity, MockOppijat.eero, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatus(200)
        }
        get("api/opiskeluoikeus/" + mitätöity.oid.get, headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
    }

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN rooli" in {
        resetFixtures
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

  def tag: TypeTag[AmmatillinenOpiskeluoikeus] = implicitly[TypeTag[AmmatillinenOpiskeluoikeus]]
  def defaultOpiskeluoikeus: AmmatillinenOpiskeluoikeus =
    oppija(MockOppijat.eero.oid, defaultUser).tallennettavatOpiskeluoikeudet.collect { case a: AmmatillinenOpiskeluoikeus => a }.head
}

package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillinenOpiskeluoikeusjakso}
import org.scalatest.{FreeSpec, Matchers}

import scala.reflect.runtime.universe.TypeTag

class OppijaGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  "/api/oppija/" - {
    "GET" - {
      "with valid oid" in {
        get("api/oppija/" + MockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with valid oid, hetuless oppija" in {
        get("api/oppija/" + MockOppijat.hetuton.oid, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with invalid oid" in {
        get("api/oppija/blerg", headers = authHeaders()) {
          verifyResponseStatus(400)
        }
      }
      "with unknown oid" in {
        get("api/oppija/1.2.246.562.24.90000000001", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.90000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
        }
      }
      "with mitätöity oid" in {
        val mitätöity = defaultOpiskeluoikeus.copy(tila = defaultOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot =
          defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
        ))
        putOpiskeluoikeus(mitätöity, MockOppijat.eero, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatus(200)
        }
        get("api/oppija/" + MockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${MockOppijat.eero.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
        }
      }
    }
  }

  def tag: TypeTag[AmmatillinenOpiskeluoikeus] = implicitly[TypeTag[AmmatillinenOpiskeluoikeus]]
  def defaultOpiskeluoikeus: AmmatillinenOpiskeluoikeus = lastOpiskeluoikeus(MockOppijat.eero.oid) match { case a: AmmatillinenOpiskeluoikeus => a }
}

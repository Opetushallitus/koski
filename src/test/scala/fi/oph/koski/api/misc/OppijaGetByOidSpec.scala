package fi.oph.koski.api.misc

import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeusjakso
import fi.oph.koski.ytr.{MockYtrClient, YtrSsnWithPreviousSsns}
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class OppijaGetByOidSpec
  extends AnyFreeSpec
    with Matchers
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethods
    with OpiskeluoikeusTestMethodsAmmatillinen
    with DirtiesFixtures {

  "/api/oppija/" - {
    "GET" - {
      "with valid oid" in {
        get("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with valid oid, hetuless oppija" in {
        get("api/oppija/" + KoskiSpecificMockOppijat.hetuton.oid, headers = authHeaders()) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with invalid oid" in {
        get("api/oppija/blerg", headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: blerg. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
      "with unknown oid" in {
        get("api/oppija/1.2.246.562.24.90000000001", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.90000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
        }
      }
      "with mitätöity oid" in {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, KoskiSpecificMockOppijat.eero)
        val mitätöity = oo.copy(tila = defaultOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot =
          defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
        ))
        putOpiskeluoikeus(mitätöity, KoskiSpecificMockOppijat.eero, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatusOk()
        }
        get("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${KoskiSpecificMockOppijat.eero.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
        }
      }
      "Hakee oppijan tiedot YTR:stä oppijan kaikilla hetuilla" in {
        KoskiApplicationForTests.cacheManager.invalidateAllCaches
        MockYtrClient.latestOppijaJsonByHetu = None

        val oppija = KoskiSpecificMockOppijat.ylioppilas
        oppija.vanhatHetut.length should be >(0)

        get("api/oppija/" + oppija.oid, headers = authHeaders()) {
          verifyResponseStatusOk()

          MockYtrClient.latestOppijaJsonByHetu should be(Some(
            YtrSsnWithPreviousSsns(oppija.hetu.get, oppija.vanhatHetut)
          ))
        }
      }
    }
  }
}

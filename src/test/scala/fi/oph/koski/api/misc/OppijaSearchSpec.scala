package fi.oph.koski.api.misc

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.omniaKatselija
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.ytr.{MockYtrClient, YtrSsnWithPreviousSsns}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppijaSearchSpec extends AnyFreeSpec with Matchers with SearchTestMethods with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  "/api/henkilo/search" - {
    "Finds by name" in {
      resetFixtures
      searchForNames("eero") should equal(List("Jouni Çelik-Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }
    "Find only those from your organization" in {
      searchForNames("eero", omniaKatselija) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
    }
    "Finds by hetu" in {
      searchForNames("010101-123N") should equal(List("Eero Esimerkki"))
    }
    "Finds ylioppilas with multiple hetus by all hetus from YTR" in {
      KoskiApplicationForTests.cacheManager.invalidateAllCaches
      MockYtrClient.latestOppijaJsonByHetu = None

      val oppija = KoskiSpecificMockOppijat.ylioppilas
      oppija.vanhatHetut.length should be >(0)

      searchForNames(oppija.hetu.get)

      MockYtrClient.latestOppijaJsonByHetu should be(Some(YtrSsnWithPreviousSsns(oppija.hetu.get, oppija.vanhatHetut)))
    }
    "Finds only those that are in Koski" in {
      searchForNames(masterEiKoskessa.hetu.get) should equal(Nil)
    }
    "Finds with master info" in {
      createOrUpdate(KoskiSpecificMockOppijat.slaveMasterEiKoskessa.henkilö, defaultOpiskeluoikeus)
      searchForNames(masterEiKoskessa.hetu.get) should equal(List("Master Master"))
    }
    "Audit logging" in {
      search("eero", defaultUser) {
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPPIJA_HAKU", "target" -> Map("hakuEhto" -> "EERO")))
      }
    }
    "When query is too short" - {
      "Returns HTTP 400" in {
        search("aa", user = defaultUser) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
    "GET endpoints" - {
      "Finds by hetu, and does not include hetu in access log" in {
        AccessLogTester.clearMessages
        authGet("api/henkilo/hetu/010101-123N") {
          verifyResponseStatusOk()
          body should include("Esimerkki")
          AccessLogTester.getLatestMatchingAccessLog("/koski/api/henkilo/hetu/") should include("/koski/api/henkilo/hetu/* HTTP")
        }
      }
      "Finds by name, and does not include name in access log" in {
        AccessLogTester.clearMessages
        authGet("api/henkilo/search?query=eero") {
          verifyResponseStatusOk()
          body should include("Eerola")
          AccessLogTester.getLatestMatchingAccessLog("/koski/api/henkilo/search") should include("/koski/api/henkilo/search?query=* HTTP")
        }
      }
      "Does not allow access for user with read-only access" in {
        AccessLogTester.clearMessages
        authGet("api/henkilo/hetu/010101-123N", omniaKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman muokkausoikeuksia"))
        }
      }
    }
    "POST hetu endpoint" - {
      "Allow access for users with write access" in {
        AccessLogTester.clearMessages
        postHenkilöHetu("010101-123N") {
          verifyResponseStatusOk()
        }
      }

      "Does not allow access for user with read-only access" in {
        AccessLogTester.clearMessages
        postHenkilöHetu("010101-123N", omniaKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman muokkausoikeuksia"))
        }
      }
    }
  }
}

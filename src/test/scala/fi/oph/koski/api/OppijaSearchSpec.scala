package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.AmmatillinenExampleData.stadinAmmattiopisto
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.omniaKatselija
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.perustiedot.{NimitiedotJaOid, OpiskeluoikeudenPerustiedot, OpiskeluoikeusJaksonPerustiedot}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos}
import org.scalatest.{FreeSpec, Matchers}

class OppijaSearchSpec extends FreeSpec with Matchers with SearchTestMethods with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  "/api/henkilo/search" - {
    "Finds by name" in {
      searchForNames("eero") should equal(List("Jouni Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }
    "Find only those from your organization" in {
      val oids = generatePerustiedotIntoElastic(10, "Jorma-Petteri", stadinAmmattiopisto)
      try {
        searchForNames("Jorma-Petteri", omniaKatselija) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
      } finally {
        KoskiApplicationForTests.perustiedotIndexer.deleteByOppijaOids(oids)
      }
    }
    "Finds by hetu" in {
      searchForNames("010101-123N") should equal(List("Eero Esimerkki"))
    }
    "Finds only those that are in Koski" in {
      searchForNames(masterEiKoskessa.hetu.get) should equal(Nil)
    }
    "Finds with master info" in {
      createOrUpdate(MockOppijat.slaveMasterEiKoskessa.henkilö, defaultOpiskeluoikeus)
      searchForNames(masterEiKoskessa.hetu.get) should equal(List("Master Master"))
    }
    "Audit logging" in {
      search("eero", defaultUser) {
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPPIJA_HAKU", "hakuEhto" -> "EERO"))
      }
    }
    "When query is missing" - {
      "Returns HTTP 400" in {
        get("api/henkilo/search", headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
    "When query is too short" - {
      "Returns HTTP 400" in {
        get("api/henkilo/search", params = List(("query" -> "aa")), headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
  }

  private def generatePerustiedotIntoElastic(count: Int, name: String, oppilaitos: Oppilaitos): List[Oid] = {
    val tyyppi = Koodistokoodiviite("ammatillinenkoulutus", "opiskeluoikeudentyyppi")
    val tiedot = 0 to count map { i =>
      val henkilöOid = s"1.2.246.562.24.000000000000$i"
      OpiskeluoikeudenPerustiedot(i, Some(NimitiedotJaOid(henkilöOid, name, name, name)), Some(henkilöOid), stadinAmmattiopisto, None, None, None, tyyppi, Nil, Some(List(OpiskeluoikeusJaksonPerustiedot(LocalDate.now, None, ExampleData.opiskeluoikeusLäsnä))), None)
    }
    KoskiApplicationForTests.perustiedotIndexer.updateBulk(tiedot, true)
    refreshElasticSearchIndex
    tiedot.map(t => t.henkilö.get.oid).toList
  }
}

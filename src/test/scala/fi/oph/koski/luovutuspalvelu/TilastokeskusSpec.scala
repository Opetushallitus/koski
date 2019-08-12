package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate.{of => date}
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.time.{LocalDate, ZonedDateTime}

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KäyttöoikeusViranomainen, MockUsers, Palvelurooli}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija, TäydellisetHenkilötiedot}
import org.scalatest.{FreeSpec, Matchers}

class TilastokeskusSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers {
  import fi.oph.koski.util.DateOrdering._
  "Tilastokeskus-API" - {
    "Vaatii TILASTOKESKUS-käyttöoikeuden" in {
      val withoutTilastokeskusAccess = MockUsers.users.filterNot(_.käyttöoikeudet.collect { case k: KäyttöoikeusViranomainen => k }.exists(_.globalPalveluroolit.contains(Palvelurooli("KOSKI", "TILASTOKESKUS"))))
      withoutTilastokeskusAccess.foreach { user =>
        authGet("api/luovutuspalvelu/haku?v=1", user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainTilastokeskus())
        }
      }
    }

    "Vaatii versionumeron" in {
      authGet("api/luovutuspalvelu/haku", MockUsers.tilastokeskusKäyttäjä) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }

    "Sivuttaa" in {
      resetFixtures
      var results = performQuery("?v=1&pageNumber=0&pageSize=5")
      results.length should equal(5)
      results.head.henkilö.asInstanceOf[TäydellisetHenkilötiedot].oid should equal(MockOppijat.defaultOppijat.minBy(_.henkilö.oid).henkilö.oid)
      results = performQuery("?v=1&pageNumber=1&pageSize=1")
      results.length should equal(1)
      results.head.henkilö.asInstanceOf[TäydellisetHenkilötiedot].oid should equal(MockOppijat.defaultOppijat.sortBy(_.henkilö.oid).drop(1).head.henkilö.oid)
    }

    "Kyselyparametrit" - {
      "päättymispäivämäärä" in {
        resetFixtures
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), MockOppijat.eero)
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), MockOppijat.teija)

        val queryString = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31&v=1"
        val oppijat = performQuery("?" + queryString)
        val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap {oppija =>
          oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].hetu.get, _))
        }
        päättymispäivät should contain(("010101-123N", LocalDate.parse("2016-01-09")))
        päättymispäivät.map(_._2).foreach { pvm => pvm should (be >= LocalDate.parse("2016-01-01") and be <= LocalDate.parse("2016-12-31"))}
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("hakuEhto" -> queryString)))
      }

      "alkamispäivämäärä" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), MockOppijat.eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), MockOppijat.teija)
        val alkamispäivät = performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeusAlkanutViimeistään=2100-01-02")
          .flatMap(_.opiskeluoikeudet.flatMap(_.alkamispäivä))
        alkamispäivät should equal(List(date(2100, 1, 2)))
      }

      "opiskeluoikeuden tyyppi" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), MockOppijat.eero)
        performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=ammatillinenkoulutus").length should equal(1)
        performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=perusopetus").length should equal(0)
      }

      "aikaleima" in {
        resetFixtures
        val now = ZonedDateTime.now

        performQuery(s"?v=1&muuttunutEnnen=${now.format(ISO_INSTANT)}").length should equal(performQuery().length)
        performQuery(s"?v=1&muuttunutJälkeen=${now.format(ISO_INSTANT)}").length should equal(0)

        performQuery(s"?v=1&muuttunutEnnen=${now.minusDays(1).format(ISO_INSTANT)}").length should equal(0)
        performQuery(s"?v=1&muuttunutJälkeen=${now.minusDays(1).format(ISO_INSTANT)}").length should equal(performQuery().length)

        insert(makeOpiskeluoikeus(LocalDate.now), MockOppijat.eero)

        val oppijat = performQuery(s"?v=1&muuttunutJälkeen=${now.format(ISO_INSTANT)}")
        oppijat.length should equal(1)
        oppijat.head.henkilö.asInstanceOf[TäydellisetHenkilötiedot].oid should equal(MockOppijat.eero.oid)
      }
    }
  }

  private def performQuery(query: String = "?v=1") = {
    authGet(s"api/luovutuspalvelu/haku$query", MockUsers.tilastokeskusKäyttäjä) {
      verifyResponseStatusOk()
      JsonSerializer.parse[List[Oppija]](body)
    }
  }

  private def insert(opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö) = putOpiskeluoikeus(opiskeluoikeus, henkilö) {
    verifyResponseStatusOk()
  }
}

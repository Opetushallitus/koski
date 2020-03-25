package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate.{of => date}
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.time.{LocalDate, ZonedDateTime}

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.{ExampleData, ExamplesLukio, LukioExampleData}
import fi.oph.koski.henkilo.MockOppijat.defaultOppijat
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.koskiuser.{KäyttöoikeusViranomainen, MockUsers, Palvelurooli}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class TilastokeskusSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers {
  import fi.oph.koski.util.DateOrdering._
  "Tilastokeskus-API" - {
    "Hakee oppijoiden tiedot" in {
      val kaikkiOppijat = performQuery()
      val master = kaikkiOppijat.find(_.henkilö.hetu == MockOppijat.master.hetu)
      master should be(defined)
      master.get.henkilö.oid should equal(MockOppijat.master.oid)
      master.get.henkilö.linkitetytOidit should equal(List(MockOppijat.slave.henkilö.oid))

      val eero = kaikkiOppijat.find(_.henkilö.hetu == MockOppijat.eero.hetu)
      eero should be(defined)
      eero.get.henkilö.oid should equal(MockOppijat.eero.oid)
      eero.get.henkilö.linkitetytOidit should be(empty)
    }

    "Hakee myös mitätöidyt opiskeluoikeudet" in {
      val kaikkiOppijat = performQuery()
      val tilat = kaikkiOppijat.flatMap(_.opiskeluoikeudet).map(_.tila.opiskeluoikeusjaksot.last.tila.koodiarvo)
      tilat should contain("mitatoity")
    }

    "Vaatii TILASTOKESKUS-käyttöoikeuden" in {
      val withoutTilastokeskusAccess = MockUsers.users.filterNot(_.käyttöoikeudet.collect { case k: KäyttöoikeusViranomainen => k }.exists(_.globalPalveluroolit.contains(Palvelurooli("KOSKI", "TILASTOKESKUS"))))
      withoutTilastokeskusAccess.foreach { user =>
        authGet("api/luovutuspalvelu/haku?v=1", user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainTilastokeskus())
        }
      }
      authGet("api/luovutuspalvelu/haku?v=1", MockUsers.tilastokeskusKäyttäjä) {
        verifyResponseStatusOk()
      }
    }

    "TILASTOKESKUS-käyttöoikeus ei toimi muualla" in {
      post("api/luovutuspalvelu/hetut", JsonSerializer.writeWithRoot(BulkHetuRequestV1(1, List(MockOppijat.eero.hetu.get), List("ammatillinenkoulutus"))), headers = authHeaders(MockUsers.tilastokeskusKäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainViranomainen())
      }
      authGet ("api/oppija", MockUsers.tilastokeskusKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet (s"api/oppija/${MockOppijat.eerola}", MockUsers.tilastokeskusKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      putOpiskeluoikeus(makeOpiskeluoikeus(date(2016, 1, 9)), headers = authHeaders(MockUsers.tilastokeskusKäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
    }

    "Vaatii versionumeron" in {
      authGet("api/luovutuspalvelu/haku", MockUsers.tilastokeskusKäyttäjä) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }

    "Sivuttaa" in {
      resetFixtures
      val total = koskeenTallennetutOppijat.length
      (3 to total by 9).foreach { pageSize =>
        var previousPage: List[(Oid, String, String, List[Oid], Seq[String])] = Nil
        0 to (total / pageSize) foreach { pageNumber =>
          val page: List[(Oid, String, String, List[Oid], Seq[String])] = performQuery(s"?v=1&pageNumber=$pageNumber&pageSize=$pageSize")
            .map(h => (h.henkilö.oid, h.henkilö.sukunimi, h.henkilö.etunimet, h.henkilö.linkitetytOidit, h.opiskeluoikeudet.map(_.oid.get)))

          page.flatMap(_._4) foreach { opiskeluoikeus =>
            previousPage.map(_._4) should not contain opiskeluoikeus
          }

          page should equal(expectedPage(pageSize, pageNumber))
          previousPage = page
        }
      }
    }

    "Kyselyparametrit" - {
      "päättymispäivämäärä" in {
        resetFixtures
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), MockOppijat.eero)
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), MockOppijat.teija)

        val queryString = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31&v=1"
        val oppijat = performQuery("?" + queryString)
        val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap {oppija =>
          oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.hetu.get, _))
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


      "opiskeluoikeuden tyypit" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), MockOppijat.eero)
        performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=ammatillinenkoulutus").flatMap(_.opiskeluoikeudet).length should equal(1)
        performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=perusopetus").flatMap(_.opiskeluoikeudet).length should equal(0)
        insert(makeLukioOpiskeluoikeus(date(2100, 1, 2)), MockOppijat.eero)
        performQuery("?v=1&opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=ammatillinenkoulutus&opiskeluoikeudenTyyppi=lukiokoulutus").flatMap(_.opiskeluoikeudet).length should equal(2)
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
        oppijat.head.henkilö.oid should equal(MockOppijat.eero.oid)
      }
    }
  }

  private val linkitettyOid: Map[Oid, Oid] = (for {
    oppija <- defaultOppijat
    masterOid <- oppija.master.map(_.oid)
  } yield masterOid -> oppija.henkilö.oid).toMap

  private val masterHenkilöt = defaultOppijat.filterNot(_.master.isDefined).map(_.henkilö).sortBy(_.oid)
  private val koskeenTallennetutOppijat: List[(Oid, String, String, List[Oid], List[String])] = masterHenkilöt.flatMap { m =>
    tryOppija(m.oid, paakayttaja) match {
      case Right(Oppija(h: TäydellisetHenkilötiedot, opiskeluoikeudet)) =>
        opiskeluoikeudet.flatMap(_.oid).map { opiskeluoikeusOid =>
          (h.oid, h.sukunimi, h.etunimet, linkitettyOid.get(h.oid).toList, List(opiskeluoikeusOid))
        }
      case _ => Nil
    }
  }

  private def expectedPage(pageSize: Int, pageNumber: Int) = {
    koskeenTallennetutOppijat.slice(pageNumber * pageSize, pageNumber * pageSize + pageSize)
  }

  private def performQuery(query: String = "?v=1") = {
    authGet(s"api/luovutuspalvelu/haku$query", MockUsers.tilastokeskusKäyttäjä) {
      verifyResponseStatusOk()
      JsonSerializer.parse[List[TilastokeskusOppija]](body)
    }
  }

  private def insert(opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö) = putOpiskeluoikeus(opiskeluoikeus, henkilö) {
    verifyResponseStatusOk()
  }

  private def makeLukioOpiskeluoikeus(alkamispäivä: LocalDate) = ExamplesLukio.päättötodistus().copy(
    tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(alku = alkamispäivä, tila = LukioExampleData.opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))))
  )
}

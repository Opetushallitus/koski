package fi.oph.koski.massaluovutus

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.documentation.ExamplesPerusopetus
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.luokallejaaneet.{MassaluovutusQueryLuokalleJaaneet, MassaluovutusQueryLuokalleJaaneetJson}
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{MassaluovutusQueryOrganisaationOpiskeluoikeudet, MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv, MassaluovutusQueryOrganisaationOpiskeluoikeudetJson, QueryOrganisaationOpiskeluoikeudetCsvDocumentation}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.massaluovutus.suorituspalvelu.{SuorituspalveluMuuttuneetJalkeenQuery, SuorituspalveluOppijaOidsQuery}
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LocalizedString, OpiskeluoikeudenTyyppi, PerusopetuksenVuosiluokanSuoritus}
import fi.oph.koski.util.Wait
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.raportit.RaportitService
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JInt, JNothing, JObject, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.net.URL
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.UUID

class MassaluovutusSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with OpiskeluoikeusTestMethodsAmmatillinen {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  override protected def beforeEach(): Unit = {
    resetFixturesSkipInvalidOpiskeluoikeudet()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  "Kyselyiden skedulointi" - {
    val user = defaultUser
    implicit val session: KoskiSpecificSession = user.toKoskiSpecificSession(app.käyttöoikeusRepository)

    def createRunningQuery(worker: String) =
      RunningQuery(
        queryId = UUID.randomUUID().toString,
        userOid = user.oid,
        query = QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
        createdAt = LocalDateTime.now().minusMinutes(1),
        startedAt = LocalDateTime.now(),
        worker = worker,
        resultFiles = List.empty,
        session = StorableSession(user).toJson,
        meta = None,
        progress = None,
      )

    "Orpo kysely vapautetaan takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val orphanedQuery = createRunningQuery("dead-worker")

        app.massaluovutusService.addRaw(orphanedQuery)
        app.massaluovutusCleanupScheduler.trigger()

        val query = app.massaluovutusService.get(UUID.fromString(orphanedQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.pending))
      }
    }

    "Toistuvasti orpoutuva kysely merkitään epäonnistuneeksi" in {
      val crashingQuery = createRunningQuery("dead-worker")
        .copy(meta = Some(QueryMeta(restarts = Some(List("1", "2", "3")))))

      app.massaluovutusService.addRaw(crashingQuery)
      app.massaluovutusCleanupScheduler.trigger()

      val query = app.massaluovutusService.get(UUID.fromString(crashingQuery.queryId))
      query.map(_.state) should equal(Right(QueryState.failed))
    }

    "Ajossa olevaa kyselyä ei vapauteta takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val worker = app.ecsMetadata.currentlyRunningKoskiInstances.head
        val runningQuery = createRunningQuery(worker.taskArn)

        app.massaluovutusService.addRaw(runningQuery)
        app.massaluovutusCleanupScheduler.trigger()

        val query = app.massaluovutusService.get(UUID.fromString(runningQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.running))
      }
    }
  }

  "Lisätiedot virhetilanteista" - {
    def createFailedQuery = FailedQuery(
        queryId = UUID.randomUUID().toString,
        userOid = MockUsers.tornioTallentaja.oid,
        query = MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
          alkanutAikaisintaan = LocalDate.of(2000, 1, 1),
        ),
        createdAt = LocalDateTime.now(),
        startedAt = LocalDateTime.now(),
        finishedAt = LocalDateTime.now(),
        worker = "ignore",
        resultFiles = List(),
        error = "Your proposed upload exceeds the maximum allowed size (Service: S3, Status Code: 400, Request ID: XYZ, Extended Request ID: xyz)",
        session = JObject(),
        meta = Some(QueryMeta(restarts = Some(List("1", "2", "3")))),
      )

    "Liian iso tulostiedosto palauttaa käyttäjälle vihjeen, mutta ei alkuperäistä virheilmoitusta" in {
      withoutRunningQueryScheduler {
        val failedQuery = createFailedQuery
        KoskiApplicationForTests.massaluovutusService.addRaw(failedQuery)
        getQuerySuccessfully(failedQuery.queryId, MockUsers.tornioTallentaja) { response =>
          val failResponse = response.asInstanceOf[FailedQueryResponse]
          failResponse.hint should equal(Some("Kyselystä syntyneen tulostiedoston koko kasvoi liian suureksi. Ehdotuksia kyselyn korjaamiseksi: rajaa kysely lyhyemmälle aikavälille; käytä tulostiedostojen ositusta asettalla format-kenttään text/x-csv-partition (kts. tarkemmat ohjeet http://localhost:7021/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat)"))
          failResponse.error should equal(None)
        }
      }
    }

    "Pääkäyttäjä näkee epäonnistuneen kyselyn alkuperäisen virheilmoituksen" in {
      withoutRunningQueryScheduler {
        val failedQuery = createFailedQuery
        KoskiApplicationForTests.massaluovutusService.addRaw(failedQuery)
        getQuerySuccessfully(failedQuery.queryId, MockUsers.paakayttaja) { response =>
          val failResponse = response.asInstanceOf[FailedQueryResponse]
          failResponse.error should equal(Some(failedQuery.error))
        }
      }
    }
  }

  "Organisaation opiskeluoikeudet" - {
    "JSON" - {
      val query = MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Ei onnistu ilman oikeuksia sensitiivisen datan lukemiseen" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.tallentajaEiLuottamuksellinen) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 21
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "alkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "Esiopetuksen lukuoikeuksilla saa vain esiopetuksen opiskeluoikeudet" in {
        val user = MockUsers.esiopetusTallentaja
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 2
      }

      "Palauttaa epäonnistuneen kyselyn, mutta jättää merkinnän auditlokiin" in {
        AuditLogTester.clearMessages()

        // Lisää rikkinäiset opiskeluoikeudet fikstureen:
        resetFixtures()

        val user = MockUsers.paakayttaja
        val queryId = addQuerySuccessfully(
          MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(
            alkanutAikaisintaan = LocalDate.of(2010, 1, 1),
            organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki)
          ), user
        ) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(
            MockOrganisaatiot.helsinginKaupunki
          )
          response.queryId
        }
        val failed = waitForFailure(queryId, user)

        failed.status shouldBe QueryState.failed
        failed.error.get should fullyMatch regex  "^Oppijan (1\\.2\\.246\\.562\\.24\\.\\d+) opiskeluoikeuden (1\\.2\\.246\\.562\\.15\\.\\d+) deserialisointi epäonnistui$".r // Näkyy pääkäyttäjälle

        // Tulokset ennen rikkinäisen opiskeluoikeuden käsittelyä on kirjoitettu vastaukseen:
        failed.files should have length 0
        failed.files.foreach(verifyResult(_, user))

        // Tulokset poikkeukseen saakka on kirjoitettu S3:een, vaikka epäonnistuneen kyselyn tuloksessa tiedostoja ei listata
        // Osoitteen arvaamalla käyttäjä voi kuitenkin saada nähtäväkseen tulokset ennen poikkeuksen lentämistä
        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "alkanutAikaisintaan=2010-01-01&organisaatio=1.2.246.562.10.346830761110",
          ),
        ))
      }
    }

    "CSV" - {
      val query = MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 5
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "alkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "Esiopetuksen lukuoikeuksilla saa vain esiopetuksen opiskeluoikeudet" in {
        val user = MockUsers.esiopetusTallentaja
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 3
        complete.files.foreach(verifyResult(_, user))
      }

      "Partitioitu kysely palauttaa oikean määrän tiedostoja" in {
        val user = MockUsers.helsinkiKatselija
        val partitionedQuery = query.copy(format = QueryFormat.csvPartition)
        val queryId = addQuerySuccessfully(partitionedQuery, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 21
        complete.files.foreach(verifyResult(_, user))
      }

      "Osasuorituksia ei palauteta, jos niistä ollaan opt-outattu" in {
        val user = MockUsers.helsinkiKatselija
        val eiOsasuorituksiaQuery = query.copy(eiOsasuorituksia = Some(true))
        val queryId = addQuerySuccessfully(eiOsasuorituksiaQuery, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files.filter(_.contains("osasuoritus")) should equal(List.empty)
        complete.files.filter(_.contains("aikajakso")) should have length 2
      }

      "Aikajaksoja ei palauteta, jos niistä ollaan opt-outattu" in {
        val user = MockUsers.helsinkiKatselija
        val eiAikajaksojaQuery = query.copy(eiAikajaksoja = Some(true))
        val queryId = addQuerySuccessfully(eiAikajaksojaQuery, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files.filter(_.contains("aikajakso")) should equal(List.empty)
        complete.files.filter(_.contains("osasuoritus")) should have length 1
      }
    }
  }

  "Päällekkäiset opiskeluoikeudet" - {
    "CSV" - {
      val query = MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(
        format = QueryFormat.csv,
        alku = LocalDate.of(2000, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_RAPORTTI",
          "target" -> Map(
            "hakuEhto" -> "alku=2000-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=1.2.246.562.10.346830761110&raportti=paallekkaisetopiskeluoikeudet",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }

    "Spreadsheet" - {
      val query = MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2000, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Valintalaskenta" - {
    val user = MockUsers.paakayttaja
    val ammattikoululainen = "1.2.246.562.24.00000000001"
    val olematon = "1.2.246.562.25.1010101010101"

    def getQuery(oid: String, rajapäivä: LocalDate = LocalDate.now()) =  ValintalaskentaQuery(
      rajapäivä = rajapäivä,
      oppijaOids = List(oid)
    )

    "Kysely ei palauta vastausta, jos oppijasta ei ole tietoja" in {
      val queryId = addQuerySuccessfully(getQuery(olematon), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      complete.files should have length 0
    }

    "Kysely hakee viimeisimmän opiskeluoikeusversion" in {
      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen), user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery].koulutusmuoto should equal(Some("ammatillinenkoulutus"))
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      complete.files.foreach(verifyResult(_, user))
    }

    "Kysely hakee opiskeluoikeusversion historiasta" in {
      val db = KoskiApplicationForTests.masterDatabase.db
      val id = QueryMethods.runDbSync(db, sql"""
        UPDATE opiskeluoikeus
        SET versionumero = 2
        WHERE oppija_oid = '1.2.246.562.24.00000000001'
          AND koulutusmuoto = 'ammatillinenkoulutus'
        RETURNING id
      """.as[Int]).head

      QueryMethods.runDbSync(db, sql"""
        INSERT INTO opiskeluoikeushistoria
          ("opiskeluoikeus_id", "aikaleima", "kayttaja_oid", "muutos", "versionumero")
          VALUES(
            $id,
            ${Timestamp.valueOf(LocalDateTime.now().plusDays(1))},
            '1.2.246.562.10.00000000001',
            '[]',
            2
          )
      """.asUpdate)

      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen), user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery].koulutusmuoto should equal(Some("ammatillinenkoulutus"))
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      verifyResultAndContent(complete.files.head, user) {
        val json = JsonMethods.parse(body)
        (json \ "opiskeluoikeudet")(0) \ "versionumero" should equal(JInt(1))
      }
    }

    "Kysely ei palauta opiskeluoikeutta, jos rajapäivä on sitä ennen" in {
      val eilen = LocalDate.now().minusDays(1)
      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen, eilen), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      complete.files should have length 0
    }

    "Duplikaattioidit siivotaan kyselystä pois" in {
      val query = ValintalaskentaQuery(
        rajapäivä = LocalDate.now(),
        oppijaOids = List(ammattikoululainen, olematon, olematon, ammattikoululainen)
      )

      val storedQuery = addQuerySuccessfully(query, user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery]
      }

      storedQuery.oppijaOids should equal(List(ammattikoululainen, olematon))
    }
  }

  "Suorituspalvelu" - {
    "Suorituspalvelukysely - aikarajan jälkeen muuttuneet" - {
      val user = MockUsers.paakayttaja

      def getQuery(muuttuneetJälkeen: LocalDateTime) = SuorituspalveluMuuttuneetJalkeenQuery(
        muuttuneetJälkeen = muuttuneetJälkeen
      )

      "Käyttöoikeudet ja logitus" - {
        val query = getQuery(LocalDateTime.now().plusHours(1))

        "Kyselyä voi käyttää pääkäyttäjänä" in {
          addQuerySuccessfully(query, MockUsers.paakayttaja) {
            _.status should equal(QueryState.pending)
          }
        }

        "Kyselyä voi käyttää oph-palvelukäyttäjänä" in {
          addQuerySuccessfully(query, MockUsers.ophkatselija) {
            _.status should equal(QueryState.pending)
          }
        }

        "Kyselyä ei voi käyttää muuna palvelukäyttäjänä" in {
          addQuery(query, MockUsers.omniaPalvelukäyttäjä) {
            verifyResponseStatus(403, KoskiErrorCategory.forbidden())
          }
        }

        "Kyselyä ei voi tehdä, jos muuttuneetEnnen on ennen muuttuneetJälkeen-ajanhetkeä" in {
          val virheellinenQuery = SuorituspalveluMuuttuneetJalkeenQuery(
            muuttuneetJälkeen = LocalDateTime.now(),
            muuttuneetEnnen = Some(LocalDateTime.now().minusHours(1)),
          )
          addQuery(virheellinenQuery, MockUsers.paakayttaja) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest("Kyselyn muuttuneetEnnen-aika ei voi olla ennen muuttuneetJälkeen-aikaa"))
          }
        }

        "Kyselystä jää audit log -merkinnät" in {
          AuditLogTester.clearMessages()
          val queryId = addQuerySuccessfully(getQuery(LocalDateTime.now().minusHours(1)), user) { response =>
            response.status should equal(QueryState.pending)
            response.queryId
          }
          val complete = waitForCompletion(queryId, user)

          verifyResultAndContent(complete.files.last, user) {
            val json = JsonMethods.parse(body)
            val oos = json.asInstanceOf[JArray]
            oos.arr.map(v =>
              (
                (v \ "oppijaOid").extract[String],
                ((v \ "opiskeluoikeudet").extract[List[JObject]].last \ "oid").extract[String])).last match {
              case (oppijaOid, opiskeluoikeusOid) => AuditLogTester.verifyLastAuditLogMessage(Map(
                "operation" -> "SUORITUSPALVELU_OPISKELUOIKEUS_HAKU",
                "target" -> Map(
                  "oppijaHenkiloOid" -> oppijaOid,
                  "opiskeluoikeusOid" -> opiskeluoikeusOid,
                ),
              ))
            }
          }
        }
      }

      "Palautuneen datan filtteröinti" - {

        def getOpiskeluoikeudet(tyyppi: Option[String] = None): List[JValue] = {
          val query = getQuery(LocalDateTime.now().minusHours(1))
          val queryId = addQuerySuccessfully(query, user) { response =>
            response.status should equal(QueryState.pending)
            response.queryId
          }
          val complete = waitForCompletion(queryId, user)

          val jsonFiles = complete.files.map { file =>
            verifyResultAndContent(file, user) {
              JsonMethods.parse(body)
            }
          }

          val oos = jsonFiles.flatMap {
            case JArray(a) => a
            case _ => Nil
          }.map(_ \ "opiskeluoikeudet").flatMap {
            case JArray(a) => a
            case _ => Nil
          }
          tyyppi match {
            case Some(t) => oos.filter(oo => (oo \ "tyyppi" \ "koodiarvo").extractOpt[String].contains(t))
            case None => oos
          }
        }

        def getSuoritukset(ooTyyppi: Option[String] = None): List[JValue] =
          getOpiskeluoikeudet(ooTyyppi)
            .map(_ \ "suoritukset")
            .collect { case JArray(list) => list }
            .flatten

        def extractStrings(values: List[JValue], path: JValue => JValue): List[String] =
          values
            .flatMap(path(_).extractOpt[String])
            .distinct
            .sorted

        def tyyppi(v: JValue) = v \ "tyyppi" \ "koodiarvo"
        def suorituksenTunniste(v: JValue) = v \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"
        def osasuoritustenMäärä(v: JValue) = v \ "osasuoritukset" match {
          case JArray(list) => Some(list.size)
          case _ => None
        }
        def viimeisinTila(v: JValue) = v \ "tila" \ "opiskeluoikeusjaksot" \ "tila" \ "koodiarvo" match {
          case JArray(list) => list.last
          case _ => JNothing
        }

        "Sisältää kaikkia tuettuja suoritustyyppejä" in {
          extractStrings(
            getSuoritukset(),
            tyyppi
          ) should equal(List(
            "aikuistenperusopetuksenoppimaara",
            "ammatillinentutkinto",
            "ammatillinentutkintoosittainen",
            "diatutkintovaihe",
            "ebtutkinto",
            "ibtutkinto",
            "internationalschooldiplomavuosiluokka",
            "lukionaineopinnot",
            "lukionoppiaineenoppimaara",
            "lukionoppimaara",
            "nuortenperusopetuksenoppiaineenoppimaara",
            "perusopetukseenvalmistavaopetus",
            "perusopetuksenoppiaineenoppimaara",
            "perusopetuksenoppimaara",
            "perusopetuksenvuosiluokka",
            "telma",
            "tuvakoulutuksensuoritus",
            "vstoppivelvollisillesuunnattukoulutus",
            "vstvapaatavoitteinenkoulutus"
          ))
        }

        "Sisältää mitätöityjä opiskeluoikeuksia" in {
          extractStrings(
            getOpiskeluoikeudet(),
            viimeisinTila
          ) should contain("mitatoity")
        }

        "Sisältää poistettuja opiskeluoikeuksia" in {
          val poistetutOpiskeluoikeudet = getOpiskeluoikeudet()
            .filter(v => (v \ "poistettu").extractOpt[Boolean].contains(true))

          poistetutOpiskeluoikeudet should not be empty

          poistetutOpiskeluoikeudet.foreach { oo =>
            (oo \ "oppijaOid") should not equal JNothing
            (oo \ "oid") should not equal JNothing
            (oo \ "versionumero") should not equal JNothing
            (oo \ "aikaleima") should not equal JNothing
          }
        }

        "Nuorten perusopetuksesta palautetaan perusopetuksen päättötodistus sekä 7., 8. ja 9. vuosiluokan suoritukset" in {
          extractStrings(
            getSuoritukset(Some("perusopetus")).filterNot(s => tyyppi(s).extract[String] == "nuortenperusopetuksenoppiaineenoppimaara"),
            suorituksenTunniste
          ) should equal(List(
            "201101", // Oppimäärän suoritus
            "7",  // Vuosiluokkien suoritukset
            "8",
            "9",
          ))
        }

        "Perusopetuksen suorituksille palautetaan oikea määrä osasuorituksia" in {
          val tunnisteetJaOsasuoritustenMäärät = getSuoritukset(Some("perusopetus"))
            .filterNot(s => tyyppi(s).extract[String] == "nuortenperusopetuksenoppiaineenoppimaara")
            .map(suoritus => suorituksenTunniste(suoritus).extract[String] -> osasuoritustenMäärä(suoritus))
            .sortBy(_._2.nonEmpty)
            .toMap

          tunnisteetJaOsasuoritustenMäärät("7") shouldBe None // 7. vuosiluokan suoritus
          tunnisteetJaOsasuoritustenMäärät("8") shouldBe None // 8. vuosiluokan suoritus
          tunnisteetJaOsasuoritustenMäärät("9") shouldBe None // 9. vuosiluokan suoritus, voi myös sisältää osasuorituksia
          tunnisteetJaOsasuoritustenMäärät("201101") shouldBe Some(23) // Oppimäärän suoritus
        }

        "Tutkintokoulutukseen valmentavan koulutuksen suorituksille palautetaan osasuoritukset ja niiden mahdolliset osasuoritukset" in {
          val tuvaSuoritukset = getSuoritukset(Some("tuva"))
          tuvaSuoritukset should not be empty

          // Päätason suorituksilla on osasuorituksia
          tuvaSuoritukset.foreach { suoritus =>
            osasuoritustenMäärä(suoritus) should not be None
          }

          // Valinnaisia osasuorituksia on olemassa
          val valinnaisetOsasuoritukset = tuvaSuoritukset
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .filter(os => (os \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo").extractOpt[String].contains("104"))
          valinnaisetOsasuoritukset.nonEmpty shouldBe true

          // Valinnaisilla osasuorituksilla on olemassa aliosasuorituksia
          valinnaisetOsasuoritukset.foreach(os =>
            osasuoritustenMäärä(os) should not be None
          )

          // Muun osan suorituksia on olemassa
          val muutOsasuoritukset = tuvaSuoritukset
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .filter(os => !(os \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo").extractOpt[String].contains("104"))
          muutOsasuoritukset.nonEmpty shouldBe true
        }

        "Ammatillisesta koulutuksesta palautetaan vain kokonainen ammatillinen tutkinto, osittainen ammatillinen tutkinto ja telma" in {
          extractStrings(
            getSuoritukset(Some("ammatillinenkoulutus")),
            tyyppi
          ) should equal(List(
            "ammatillinentutkinto",
            "ammatillinentutkintoosittainen",
            "telma"
          ))
        }

        "DIA-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("diatutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "DIA-tutkinnoista palautetaan osasuoritukset ja alaosasuoritukset" in {
          val alaosasuoritukset = getSuoritukset(Some("diatutkinto"))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))

          extractStrings(
            alaosasuoritukset,
            suorituksenTunniste
          ) should equal(List(
            "3",
            "4",
            "5",
            "6",
            "kirjallinenkoe",
          ))
        }

        "EB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("ebtutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "EB-tutkinnoista palautetaan osasuoritukset ja alaosasuoritukset" in {
          val alaosasuoritukset = getSuoritukset(Some("ebtutkinto"))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))

          extractStrings(
            alaosasuoritukset,
            suorituksenTunniste
          ) should equal(List(
            "Final",
            "Oral",
            "Written"
          ))
        }

        "IB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("ibtutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "Opiskeluoikeudet sisältävät versionumeron ja aikaleiman sekä opiskeluoikeuden alkamis- ja päättymispäivän" in {
          val oos = getOpiskeluoikeudet()
            .filterNot(v => (v \ "poistettu").extractOpt[Boolean].contains(true))
          oos should not be empty
          oos.foreach { oo =>
            (oo \ "versionumero") should not equal JNothing
            (oo \ "aikaleima") should not equal JNothing
            (oo \ "alkamispäivä") should not equal JNothing
          }
          oos.exists { oo =>
            (oo \ "päättymispäivä") != JNothing
          } shouldBe true
        }
      }

      "Palauttaa oppijan kaikki opiskeluoikeudet jos yksikin on muuttunut" in {
        val oppija = KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia
        val oo = getOpiskeluoikeus(oppija.oid, "perusopetus")
        val muokattuOo = oo.withSuoritukset(oo.suoritukset.map {
          case p: PerusopetuksenVuosiluokanSuoritus => p.copy(todistuksellaNäkyvätLisätiedot = Some(LocalizedString.finnish("asd")))
          case s => s
        })
        createOrUpdate(oppija, muokattuOo)
        val tallennettuOo = getOpiskeluoikeus(muokattuOo.oid.get).asInstanceOf[KoskeenTallennettavaOpiskeluoikeus]

        val query = getQuery(tallennettuOo.aikaleima.get)
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }
        val oppijat = jsonFiles.head.extract[Seq[JObject]]
        oppijat.length should equal(1)
        (oppijat.head \ "opiskeluoikeudet").extract[Seq[JObject]].length should equal(4)
      }

      "muuttuneetEnnen rajaa tuloksia aikaikkunan mukaan" in {
        val oppija = KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia
        poistaOppijanOpiskeluoikeusDatat(oppija)
        val tallennettuOo = createOrUpdate(oppija, defaultOpiskeluoikeus)
        val aikaleima = tallennettuOo.aikaleima.get

        // Haku aikaikkunalla, joka sisältää opiskeluoikeuden aikaleiman, palauttaa oppijan
        val queryIdSisältyy = addQuerySuccessfully(
          SuorituspalveluMuuttuneetJalkeenQuery(
            muuttuneetJälkeen = aikaleima,
            muuttuneetEnnen = Some(aikaleima),
          ),
          user
        )(_.queryId)
        val completeSisältyy = waitForCompletion(queryIdSisältyy, user)
        val jsonFilesSisältyy = completeSisältyy.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }
        val oppijatSisältyy = jsonFilesSisältyy.head.extract[Seq[JObject]]
        oppijatSisältyy.length should equal(1)
        (oppijatSisältyy.head \ "oppijaOid").extract[String] should equal(oppija.oid)

        // Haku aikaikkunalla, jossa muuttuneetEnnen rajaa ulos opiskeluoikeuden aikaleiman
        val queryIdEiSisälly = addQuerySuccessfully(
          SuorituspalveluMuuttuneetJalkeenQuery(
            muuttuneetJälkeen = LocalDateTime.now().minusHours(1),
            muuttuneetEnnen = Some(aikaleima.minusSeconds(1)),
          ),
          user
        )(_.queryId)
        val completeEiSisälly = waitForCompletion(queryIdEiSisälly, user)

        val jsonFilesEiSisälly = completeEiSisälly.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        // Tulokset eivät sisällä muuttuneen opiskeluoikeuden oppijaa
        jsonFilesEiSisälly.foreach { jsonFile =>
          val oppijatJson = jsonFile.extract[Seq[JObject]]
          oppijatJson.foreach (oppijaJson => (oppijaJson \ "oppijaOid").extract[String] should not equal(oppija.oid))
        }
      }

      "Palauttaa myös master oid:n opiskeluoikeuden jos slave oid:n opiskeluoikeus on muuttunut" in {
        poistaOppijanOpiskeluoikeusDatat(KoskiSpecificMockOppijat.slave.henkilö)
        val tallennettuOo = createOrUpdate(KoskiSpecificMockOppijat.slave.henkilö, defaultOpiskeluoikeus)

        val query = getQuery(tallennettuOo.aikaleima.get)
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        val oppijatJaOpiskeluoikeudenTyypit = (jsonFiles.head.extract[Seq[JObject]].head \ "opiskeluoikeudet")
          .extract[Seq[JObject]]
          .map(oo => (oo \ "oppijaOid").extract[String] -> (oo \ "tyyppi" \ "koodiarvo").extract[String])

        oppijatJaOpiskeluoikeudenTyypit should contain(
          KoskiSpecificMockOppijat.slave.henkilö.oid -> OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo
        )
        oppijatJaOpiskeluoikeudenTyypit should contain(
          KoskiSpecificMockOppijat.master.oid -> OpiskeluoikeudenTyyppi.perusopetus.koodiarvo
        )
      }

      "Palauttaa virheeseen päätyneitä opiskeluoikeuksia" in {
        // Lisää rikkinäiset opiskeluoikeudet fikstureen:
        resetFixtures()

        val queryId = addQuerySuccessfully(getQuery(LocalDateTime.now().minusHours(1)), user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }

        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        val oppijat = jsonFiles.head.extract[Seq[JObject]]
        val virheelliset = oppijat.filter(oppija => (oppija \ "virheellisetOpiskeluoikeudet").extract[Seq[JObject]].nonEmpty)

        virheelliset should not be empty
        virheelliset.foreach { oppija =>
          val virheellisetOpiskeluoikeudet = (oppija \ "virheellisetOpiskeluoikeudet").extract[Seq[JObject]]
          virheellisetOpiskeluoikeudet.foreach { oo =>
            (oo  \ "oppijaOid") should not equal JNothing
            (oo \ "oid") should not equal JNothing
            (oo \ "versionumero") should not equal JNothing
            (oo \ "virheet").extract[List[String]] should not be empty
          }
        }
      }
    }

    "Suorituspalvelukysely - oppija-oideilla hakeminen" - {
      val user = MockUsers.paakayttaja
      val oppijaOids = (1 to 200).map(i => "1.2.246.562.24.00000000%03d".format(i))

      def getQuery(oppijaOidit: Seq[String]) = SuorituspalveluOppijaOidsQuery(
        oppijaOids = oppijaOidit
      )

      "Käyttöoikeudet ja logitus" - {
        val query = getQuery(oppijaOids)

        "Kyselyä voi käyttää pääkäyttäjänä" in {
          addQuerySuccessfully(query, MockUsers.paakayttaja) {
            _.status should equal(QueryState.pending)
          }
        }

        "Kyselyä voi käyttää oph-palvelukäyttäjänä" in {
          addQuerySuccessfully(query, MockUsers.ophkatselija) {
            _.status should equal(QueryState.pending)
          }
        }

        "Kyselyä ei voi käyttää muuna palvelukäyttäjänä" in {
          addQuery(query, MockUsers.omniaPalvelukäyttäjä) {
            verifyResponseStatus(403, KoskiErrorCategory.forbidden())
          }
        }

        "Kyselystä jää audit log -merkinnät" in {
          AuditLogTester.clearMessages()
          val queryId = addQuerySuccessfully(getQuery(oppijaOids), user) { response =>
            response.status should equal(QueryState.pending)
            response.queryId
          }
          val complete = waitForCompletion(queryId, user)

          verifyResultAndContent(complete.files.last, user) {
            val json = JsonMethods.parse(body)
            val oos = json.asInstanceOf[JArray]
            oos.arr.map(v =>
              (
                (v \ "oppijaOid").extract[String],
                (v \ "opiskeluoikeudet").extract[List[JObject]].lastOption.map(oo => (oo \ "oid").extract[String]).getOrElse("")
              )
            ).last match {
              case (oppijaOid, opiskeluoikeusOid) => AuditLogTester.verifyLastAuditLogMessage(Map(
                "operation" -> "SUORITUSPALVELU_OPISKELUOIKEUS_HAKU",
                "target" -> Map(
                  "oppijaHenkiloOid" -> oppijaOid,
                  "opiskeluoikeusOid" -> opiskeluoikeusOid,
                ),
              ))
            }
          }
        }
      }

      "Palautuneen datan filtteröinti" - {

        def getOpiskeluoikeudet(tyyppi: Option[String] = None): List[JValue] = {
          val query = getQuery(oppijaOids)
          val queryId = addQuerySuccessfully(query, user) { response =>
            response.status should equal(QueryState.pending)
            response.queryId
          }
          val complete = waitForCompletion(queryId, user)

          val jsonFiles = complete.files.map { file =>
            verifyResultAndContent(file, user) {
              JsonMethods.parse(body)
            }
          }

          val oos = jsonFiles.flatMap {
            case JArray(a) => a
            case _ => Nil
          }.map(_ \ "opiskeluoikeudet").flatMap {
            case JArray(a) => a
            case _ => Nil
          }
          tyyppi match {
            case Some(t) => oos.filter(oo => (oo \ "tyyppi" \ "koodiarvo").extractOpt[String].contains(t))
            case None => oos
          }
        }

        def getSuoritukset(ooTyyppi: Option[String] = None): List[JValue] =
          getOpiskeluoikeudet(ooTyyppi)
            .map(_ \ "suoritukset")
            .collect { case JArray(list) => list }
            .flatten

        def extractStrings(values: List[JValue], path: JValue => JValue): List[String] =
          values
            .flatMap(path(_).extractOpt[String])
            .distinct
            .sorted

        def tyyppi(v: JValue) = v \ "tyyppi" \ "koodiarvo"

        def suorituksenTunniste(v: JValue) = v \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"

        def osasuoritustenMäärä(v: JValue) = v \ "osasuoritukset" match {
          case JArray(list) => Some(list.size)
          case _ => None
        }

        def viimeisinTila(v: JValue) = v \ "tila" \ "opiskeluoikeusjaksot" \ "tila" \ "koodiarvo" match {
          case JArray(list) => list.last
          case _ => JNothing
        }

        "Sisältää kaikkia tuettuja suoritustyyppejä" in {
          extractStrings(
            getSuoritukset(),
            tyyppi
          ) should equal(List(
            "aikuistenperusopetuksenoppimaara",
            "ammatillinentutkinto",
            "ammatillinentutkintoosittainen",
            "diatutkintovaihe",
            "ebtutkinto",
            "ibtutkinto",
            "internationalschooldiplomavuosiluokka",
            "lukionaineopinnot",
            "lukionoppiaineenoppimaara",
            "lukionoppimaara",
            "nuortenperusopetuksenoppiaineenoppimaara",
            "perusopetukseenvalmistavaopetus",
            "perusopetuksenoppiaineenoppimaara",
            "perusopetuksenoppimaara",
            "perusopetuksenvuosiluokka",
            "telma",
            "tuvakoulutuksensuoritus",
            "vstoppivelvollisillesuunnattukoulutus",
            "vstvapaatavoitteinenkoulutus"
          ))
        }

        "Sisältää mitätöityjä opiskeluoikeuksia" in {
          extractStrings(
            getOpiskeluoikeudet(),
            viimeisinTila
          ) should contain("mitatoity")
        }

        "Sisältää poistettuja opiskeluoikeuksia" in {
          val poistetutOpiskeluoikeudet = getOpiskeluoikeudet()
            .filter(v => (v \ "poistettu").extractOpt[Boolean].contains(true))

          poistetutOpiskeluoikeudet should not be empty

          poistetutOpiskeluoikeudet.foreach { oo =>
            (oo \ "oppijaOid") should not equal JNothing
            (oo \ "oid") should not equal JNothing
            (oo \ "versionumero") should not equal JNothing
            (oo \ "aikaleima") should not equal JNothing
          }
        }

        "Nuorten perusopetuksesta palautetaan perusopetuksen päättötodistus sekä 7., 8. ja 9. vuosiluokan suoritukset" in {
          extractStrings(
            getSuoritukset(Some("perusopetus")).filterNot(s => tyyppi(s).extract[String] == "nuortenperusopetuksenoppiaineenoppimaara"),
            suorituksenTunniste
          ) should equal(List(
            "201101", // Oppimäärän suoritus
            "7",  // Vuosiluokkien suoritukset
            "8",
            "9",
          ))
        }

        "Perusopetuksen suorituksille palautetaan oikea määrä osasuorituksia" in {
          val tunnisteetJaOsasuoritustenMäärät = getSuoritukset(Some("perusopetus"))
            .filterNot(s => tyyppi(s).extract[String] == "nuortenperusopetuksenoppiaineenoppimaara")
            .map(suoritus => suorituksenTunniste(suoritus).extract[String] -> osasuoritustenMäärä(suoritus))
            .sortBy(_._2.nonEmpty)
            .toMap

          tunnisteetJaOsasuoritustenMäärät("7") shouldBe None // 7. vuosiluokan suoritus
          tunnisteetJaOsasuoritustenMäärät("8") shouldBe None // 8. vuosiluokan suoritus
          tunnisteetJaOsasuoritustenMäärät("9") shouldBe None // 9. vuosiluokan suoritus, voi myös sisältää osasuorituksia
          tunnisteetJaOsasuoritustenMäärät("201101") shouldBe Some(23) // Oppimäärän suoritus
        }

        "Tutkintokoulutukseen valmentavan koulutuksen suorituksille palautetaan osasuoritukset ja niiden mahdolliset osasuoritukset" in {
          val tuvaSuoritukset = getSuoritukset(Some("tuva"))
          tuvaSuoritukset should not be empty

          // Päätason suorituksilla on osasuorituksia
          tuvaSuoritukset.foreach { suoritus =>
            osasuoritustenMäärä(suoritus) should not be None
          }

          // Valinnaisia osasuorituksia on olemassa
          val valinnaisetOsasuoritukset = tuvaSuoritukset
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .filter(os => (os \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo").extractOpt[String].contains("104"))
          valinnaisetOsasuoritukset.nonEmpty shouldBe true

          // Valinnaisilla osasuorituksilla on olemassa aliosasuorituksia
          valinnaisetOsasuoritukset.foreach(os =>
            osasuoritustenMäärä(os) should not be None
          )

          // Muun osan suorituksia on olemassa
          val muutOsasuoritukset = tuvaSuoritukset
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .filter(os => !(os \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo").extractOpt[String].contains("104"))
          muutOsasuoritukset.nonEmpty shouldBe true
        }

        "Ammatillisesta koulutuksesta palautetaan vain kokonainen ammatillinen tutkinto, osittainen ammatillinen tutkinto ja telma" in {
          extractStrings(
            getSuoritukset(Some("ammatillinenkoulutus")),
            tyyppi
          ) should equal(List(
            "ammatillinentutkinto",
            "ammatillinentutkintoosittainen",
            "telma"
          ))
        }

        "DIA-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("diatutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "DIA-tutkinnoista palautetaan osasuoritukset ja alaosasuoritukset" in {
          val alaosasuoritukset = getSuoritukset(Some("diatutkinto"))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))

          extractStrings(
            alaosasuoritukset,
            suorituksenTunniste
          ) should equal(List(
            "3",
            "4",
            "5",
            "6",
            "kirjallinenkoe",
          ))
        }

        "EB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("ebtutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "EB-tutkinnoista palautetaan osasuoritukset ja alaosasuoritukset" in {
          val alaosasuoritukset = getSuoritukset(Some("ebtutkinto"))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))
            .flatMap(s => (s \ "osasuoritukset").extractOpt[List[JObject]].getOrElse(Nil))

          extractStrings(
            alaosasuoritukset,
            suorituksenTunniste
          ) should equal(List(
            "Final",
            "Oral",
            "Written"
          ))
        }

        "IB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
          extractStrings(
            getOpiskeluoikeudet(Some("ibtutkinto")),
            viimeisinTila
          ) should equal(List(
            "valmistunut"
          ))
        }

        "Opiskeluoikeudet sisältävät versionumeron ja aikaleiman sekä opiskeluoikeuden alkamis- ja päättymispäivän" in {
          val oos = getOpiskeluoikeudet()
            .filterNot(v => (v \ "poistettu").extractOpt[Boolean].contains(true))
          oos should not be empty
          oos.foreach { oo =>
            (oo \ "versionumero") should not equal JNothing
            (oo \ "aikaleima") should not equal JNothing
            (oo \ "alkamispäivä") should not equal JNothing
          }
          oos.exists { oo =>
            (oo \ "päättymispäivä") != JNothing
          } shouldBe true
        }
      }

      "Haku slave oidilla palauttaa master-oppijan" in {
        val query = getQuery(Seq(KoskiSpecificMockOppijat.slave.henkilö.oid))
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        (jsonFiles.head.extract[Seq[JObject]].head \ "oppijaOid").extract[String] should equal(KoskiSpecificMockOppijat.master.oid)
      }

      "Haku master oidilla palauttaa oppijan slave-oidineen" in {
        val query = getQuery(Seq(KoskiSpecificMockOppijat.master.oid))
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        (jsonFiles.head.extract[Seq[JObject]].head \ "kaikkiOidit").extract[Set[String]] should equal(Set(KoskiSpecificMockOppijat.master.oid, KoskiSpecificMockOppijat.slave.henkilö.oid))
      }

      "Haku master- ja slave oidilla samaan aikaan palauttaa vain yhden oppijan" in {
        val query = getQuery(Seq(KoskiSpecificMockOppijat.master.oid, KoskiSpecificMockOppijat.slave.henkilö.oid))
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        jsonFiles.head.extract[Seq[JObject]].length should equal(1)
      }

      "Palautetulla opiskeluoikeudella on sen oppijan oid, jolle opiskeluoikeus on tallennettu" in {
        poistaOppijanOpiskeluoikeusDatat(KoskiSpecificMockOppijat.slave.henkilö)
        createOrUpdate(KoskiSpecificMockOppijat.slave.henkilö, defaultOpiskeluoikeus)

        val query = getQuery(Seq(KoskiSpecificMockOppijat.master.oid))
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        val oppijatJaOpiskeluoikeudenTyypit = (jsonFiles.head.extract[Seq[JObject]].head \ "opiskeluoikeudet")
          .extract[Seq[JObject]]
          .map(oo => (oo \ "oppijaOid").extract[String] -> (oo \ "tyyppi" \ "koodiarvo").extract[String])

        oppijatJaOpiskeluoikeudenTyypit should contain(
          KoskiSpecificMockOppijat.slave.henkilö.oid -> OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo
        )
        oppijatJaOpiskeluoikeudenTyypit should contain(
          KoskiSpecificMockOppijat.master.oid -> OpiskeluoikeudenTyyppi.perusopetus.koodiarvo
        )
      }

      "Ei palauta tyhjää listaa oppijoista" in {
        poistaOppijanOpiskeluoikeusDatat(KoskiSpecificMockOppijat.tero)
        createOrUpdate(KoskiSpecificMockOppijat.tero, ExamplesPerusopetus.kuudennenLuokanOsaAikainenErityisopetusOpiskeluoikeus)

        val query = getQuery(Seq(KoskiSpecificMockOppijat.tero.oid))
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)
        complete.files shouldBe empty
      }

      "Palauttaa virheeseen päätyneitä opiskeluoikeuksia" in {
        // Lisää rikkinäiset opiskeluoikeudet fikstureen:
        resetFixtures()

        val queryId = addQuerySuccessfully(getQuery(oppijaOids), user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }

        val complete = waitForCompletion(queryId, user)

        val jsonFiles = complete.files.map { file =>
          verifyResultAndContent(file, user) {
            JsonMethods.parse(body)
          }
        }

        val oppijat = jsonFiles.head.extract[Seq[JObject]]
        val virheelliset = oppijat.filter(oppija => (oppija \ "virheellisetOpiskeluoikeudet").extract[Seq[JObject]].nonEmpty)

        virheelliset should not be empty
        virheelliset.foreach { oppija =>
          val virheellisetOpiskeluoikeudet = (oppija \ "virheellisetOpiskeluoikeudet").extract[Seq[JObject]]
          virheellisetOpiskeluoikeudet.foreach { oo =>
            (oo  \ "oppijaOid") should not equal JNothing
            (oo \ "oid") should not equal JNothing
            (oo \ "versionumero") should not equal JNothing
            (oo \ "virheet").extract[List[String]] should not be empty
          }
        }
      }
    }
  }


  "Luokalle jäämiset" - {
    "JSON" - {
      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = None), MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Ei onnistu ilman oikeuksia sensitiivisen datan lukemiseen" in {
        addQuery(MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), MockUsers.tallentajaEiLuottamuksellinen) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = None), user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryLuokalleJaaneet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 2
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_KATSOMINEN",
          "target" -> Map(
            "oppijaHenkiloOid" -> KoskiSpecificMockOppijat.perusopetuksenTiedonsiirto.oid,
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = None), MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }
  }
}

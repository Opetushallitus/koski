package fi.oph.koski.massaluovutus

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.luokallejaaneet.{MassaluovutusQueryLuokalleJaaneet, MassaluovutusQueryLuokalleJaaneetJson}
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{MassaluovutusQueryOrganisaationOpiskeluoikeudet, MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv, MassaluovutusQueryOrganisaationOpiskeluoikeudetJson, QueryOrganisaationOpiskeluoikeudetCsvDocumentation}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.massaluovutus.suoritusrekisteri.{SuoritusrekisteriMuuttuneetJalkeenQuery, SuoritusrekisteriOppijaOidsQuery}
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LocalizedString, PerusopetuksenVuosiluokanSuoritus}
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JInt, JNothing, JObject, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.net.URL
import java.sql.Timestamp
import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.UUID

class MassaluovutusSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with OpiskeluoikeusTestMethodsAmmatillinen {
  val app = KoskiApplicationForTests

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  override protected def afterEach(): Unit = {
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

        complete.files should have length 20
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

        complete.files should have length 19
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
        val json = JsonMethods.parse(response.body)
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

  "Suoritusrekisterikysely - aikarajan jälkeen muuttuneet" - {
    val user = MockUsers.paakayttaja

    def getQuery(muuttuneetJälkeen: LocalDateTime) = SuoritusrekisteriMuuttuneetJalkeenQuery(
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

      "Kyselystä jää audit log -merkinnät" in {
        AuditLogTester.clearMessages()
        val queryId = addQuerySuccessfully(getQuery(LocalDateTime.now().minusHours(1)), user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        verifyResultAndContent(complete.files.last, user) {
          val json = JsonMethods.parse(response.body)
          val oos = json.asInstanceOf[JArray]
          oos.arr.map(v =>
            (
              (v \ "oppijaOid").extract[String],
              ((v \ "opiskeluoikeudet").extract[List[JObject]].last \ "oid").extract[String])).last match {
            case (oppijaOid, opiskeluoikeusOid) => AuditLogTester.verifyLastAuditLogMessage(Map(
              "operation" -> "SUORITUSREKISTERI_OPISKELUOIKEUS_HAKU",
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
      val query = getQuery(LocalDateTime.now().minusHours(1))
      val queryId = addQuerySuccessfully(query, user) { response =>
        response.status should equal(QueryState.pending)
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      val jsonFiles = complete.files.map { file =>
        verifyResultAndContent(file, user) {
          JsonMethods.parse(response.body)
        }
      }

      def getOpiskeluoikeudet(tyyppi: Option[String] = None): List[JValue] = {
        val oos = jsonFiles.flatMap {
          case JArray(a) => a
          case _ => Nil
        }.map(_ \ "opiskeluoikeudet").flatMap {
          case JArray(a) => a
          case _ => Nil
        }
        tyyppi match {
          case Some(t) => oos.filter(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String] == t)
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
          .map(path(_).extract[String])
          .distinct
          .sorted

      def tyyppi(v: JValue) = v \ "tyyppi" \ "koodiarvo"
      def suorituksenTunniste(v: JValue) = v \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"
      def osasuoritustenMäärä(v: JValue) = v \ "osasuoritukset" match {
        case JArray(list) => list.size
        case _ => 0
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
          "diatutkintovaihe",
          "ebtutkinto",
          "ibtutkinto",
          "internationalschooldiplomavuosiluokka",
          "nuortenperusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppimaara",
          "perusopetuksenvuosiluokka",
          "telma",
          "tuvakoulutuksensuoritus",
          "vstoppivelvollisillesuunnattukoulutus",
        ))
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
          .toMap

        tunnisteetJaOsasuoritustenMäärät.get("7") shouldBe Some(0) // 7. vuosiluokan suoritus
        tunnisteetJaOsasuoritustenMäärät.get("8") shouldBe Some(0) // 8. vuosiluokan suoritus
        tunnisteetJaOsasuoritustenMäärät.get("9") shouldBe Some(0) // 9. vuosiluokan suoritus, voi myös sisältää osasuorituksia
        tunnisteetJaOsasuoritustenMäärät.get("201101") shouldBe Some(23) // Oppimäärän suoritus
      }

      "Ammatillisesta koulutuksesta ei palautetaan vain kokonainen ammatillinen tutkinto ja telma" in {
        extractStrings(
          getSuoritukset(Some("ammatillinenkoulutus")),
          tyyppi
        ) should equal(List(
          "ammatillinentutkinto",
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

      "EB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
        extractStrings(
          getOpiskeluoikeudet(Some("ebtutkinto")),
          viimeisinTila
        ) should equal(List(
          "valmistunut"
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
          JsonMethods.parse(response.body)
        }
      }
      val oppijat = jsonFiles.head.extract[Seq[JObject]]
      oppijat.length should equal(1)
      (oppijat.head \ "opiskeluoikeudet").extract[Seq[JObject]].length should equal(7)
    }
  }

  "Suoritusrekisterikysely - oppija-oideilla hakeminen" - {
    val user = MockUsers.paakayttaja
    val oppijaOids = (1 to 200).map(i => "1.2.246.562.24.00000000%03d".format(i))

    def getQuery(oppijaOidit: Seq[String]) = SuoritusrekisteriOppijaOidsQuery(
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
          val json = JsonMethods.parse(response.body)
          val oos = json.asInstanceOf[JArray]
          oos.arr.map(v =>
            (
              (v \ "oppijaOid").extract[String],
              ((v \ "opiskeluoikeudet").extract[List[JObject]].last \ "oid").extract[String])).last match {
            case (oppijaOid, opiskeluoikeusOid) => AuditLogTester.verifyLastAuditLogMessage(Map(
              "operation" -> "SUORITUSREKISTERI_OPISKELUOIKEUS_HAKU",
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
      val query = getQuery(oppijaOids)
      val queryId = addQuerySuccessfully(query, user) { response =>
        response.status should equal(QueryState.pending)
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      val jsonFiles = complete.files.map { file =>
        verifyResultAndContent(file, user) {
          JsonMethods.parse(response.body)
        }
      }

      def getOpiskeluoikeudet(tyyppi: Option[String] = None): List[JValue] = {
        val oos = jsonFiles.flatMap {
          case JArray(a) => a
          case _ => Nil
        }.map(_ \ "opiskeluoikeudet").flatMap {
          case JArray(a) => a
          case _ => Nil
        }
        tyyppi match {
          case Some(t) => oos.filter(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String] == t)
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
          .map(path(_).extract[String])
          .distinct
          .sorted

      def tyyppi(v: JValue) = v \ "tyyppi" \ "koodiarvo"

      def suorituksenTunniste(v: JValue) = v \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"

      def osasuoritustenMäärä(v: JValue) = v \ "osasuoritukset" match {
        case JArray(list) => list.size
        case _ => 0
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
          "diatutkintovaihe",
          "ebtutkinto",
          "ibtutkinto",
          "internationalschooldiplomavuosiluokka",
          "nuortenperusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppimaara",
          "perusopetuksenvuosiluokka",
          "telma",
          "tuvakoulutuksensuoritus",
          "vstoppivelvollisillesuunnattukoulutus",
        ))
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
          .toMap

        tunnisteetJaOsasuoritustenMäärät.get("7") shouldBe Some(0) // 7. vuosiluokan suoritus
        tunnisteetJaOsasuoritustenMäärät.get("8") shouldBe Some(0) // 8. vuosiluokan suoritus
        tunnisteetJaOsasuoritustenMäärät.get("9") shouldBe Some(0) // 9. vuosiluokan suoritus, voi myös sisältää osasuorituksia
        tunnisteetJaOsasuoritustenMäärät.get("201101") shouldBe Some(23) // Oppimäärän suoritus
      }

      "Ammatillisesta koulutuksesta ei palautetaan vain kokonainen ammatillinen tutkinto ja telma" in {
        extractStrings(
          getSuoritukset(Some("ammatillinenkoulutus")),
          tyyppi
        ) should equal(List(
          "ammatillinentutkinto",
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

      "EB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
        extractStrings(
          getOpiskeluoikeudet(Some("ebtutkinto")),
          viimeisinTila
        ) should equal(List(
          "valmistunut"
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
          JsonMethods.parse(response.body)
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
          JsonMethods.parse(response.body)
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
          JsonMethods.parse(response.body)
        }
      }

      jsonFiles.head.extract[Seq[JObject]].length should equal(1)
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


  def addQuery[T](query: MassaluovutusQueryParameters, user: UserWithPassword)(f: => T): T =
    post("api/massaluovutus", JsonSerializer.writeWithRoot(query), headers = authHeaders(user) ++ jsonContent)(f)

  def addQuerySuccessfully[T](query: MassaluovutusQueryParameters, user: UserWithPassword)(f: QueryResponse => T): T = {
    addQuery(query, user) {
      f(parsedResponse)
    }
  }

  def getQuery[T](queryId: String, user: UserWithPassword)(f: => T): T =
    get(s"api/massaluovutus/$queryId", headers = authHeaders(user) ++ jsonContent)(f)

  def getQuerySuccessfully[T](queryId: String, user: UserWithPassword)(f: QueryResponse => T): T = {
    getQuery(queryId, user) {
      f(parsedResponse)
    }
  }

  def getResult[T](url: String, user: UserWithPassword)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = authHeaders(user))(f)
  }

  def verifyResult(url: String, user: UserWithPassword): Unit =
    getResult(url, user) {
      verifyResponseStatus(302) // 302: Found (redirect)
    }

  def verifyResultAndContent[T](url: String, user: UserWithPassword)(f: => T): T = {
    val location = new URL(getResult(url, user) {
      verifyResponseStatus(302) // 302: Found (redirect)
      response.header("Location")
    })
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  def waitForStateTransition(queryId: String, user: UserWithPassword)(states: String*): QueryResponse = {
    var lastResponse: Option[QueryResponse] = None
    Wait.until {
      getQuerySuccessfully(queryId, user) { response =>
        states should contain(response.status)
        lastResponse = Some(response)
        response.status == states.last
      }
    }
    lastResponse.get
  }

  def waitForCompletion(queryId: String, user: UserWithPassword): CompleteQueryResponse =
    waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

  def parsedResponse: QueryResponse = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[QueryResponse](json, strictDeserialization)
    result should not be Left
    result.right.get
  }

  def withoutRunningQueryScheduler[T](f: => T): T =
    try {
      app.massaluovutusScheduler.pause(Duration.ofDays(1))
      f
    } finally {
      app.massaluovutusService.truncate
      app.massaluovutusScheduler.resume()
    }
}

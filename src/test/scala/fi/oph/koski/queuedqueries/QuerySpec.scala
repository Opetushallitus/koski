package fi.oph.koski.queuedqueries

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_HAKU
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.ressunLukio
import fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.{QueryOrganisaationOpiskeluoikeudet, QueryOrganisaationOpiskeluoikeudetCsv, QueryOrganisaationOpiskeluoikeudetCsvDocumentation, QueryOrganisaationOpiskeluoikeudetJson}
import fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.UUID

class QuerySpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll {
  val app = KoskiApplicationForTests

  override protected def beforeAll(): Unit = {
    resetFixtures()
    app.kyselyService.cancelAllTasks("cleanup")
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
        session = StorableSession(user).toJson,
        meta = None,
      )

    "Orpo kysely vapautetaan takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val orphanedQuery = createRunningQuery("dead-worker")

        app.kyselyService.addRaw(orphanedQuery)
        app.kyselyCleanupScheduler.trigger()

        val query = app.kyselyService.get(UUID.fromString(orphanedQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.pending))
      }
    }

    "Toistuvasti orpoutuva kysely merkitään epäonnistuneeksi" in {
      val crashingQuery = createRunningQuery("dead-worker")
        .copy(meta = Some(QueryMeta(restarts = Some(List("1", "2", "3")))))

      app.kyselyService.addRaw(crashingQuery)
      app.kyselyCleanupScheduler.trigger()

      val query = app.kyselyService.get(UUID.fromString(crashingQuery.queryId))
      query.map(_.state) should equal(Right(QueryState.failed))
    }

    "Ajossa olevaa kyselyä ei vapauteta takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val worker = app.ecsMetadata.currentlyRunningKoskiInstances.head
        val runningQuery = createRunningQuery(worker.taskArn)

        app.kyselyService.addRaw(runningQuery)
        app.kyselyCleanupScheduler.trigger()

        val query = app.kyselyService.get(UUID.fromString(runningQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.running))
      }
    }
  }

  "Organisaation opiskeluoikeudet" - {
    "JSON" - {
      val query = QueryOrganisaationOpiskeluoikeudetJson(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.kyselyt.eiYksiselitteinenOrganisaatio())
        }
      }

      "Ei onnistu ilman oikeuksia sensitiivisen datan lukemiseen" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.tallentajaEiLuottamuksellinen) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[QueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 14
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "opiskeluoikeusAlkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
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
          response.query.asInstanceOf[QueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
      }
    }

    "CSV" - {
      val query = QueryOrganisaationOpiskeluoikeudetCsv(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.kyselyt.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[QueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 5
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "opiskeluoikeusAlkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
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
          response.query.asInstanceOf[QueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 3
        complete.files.foreach(verifyResult(_, user))
      }
    }
  }

  "Päällekkäiset opiskeluoikeudet" - {
    "CSV" - {
      val query = QueryPaallekkaisetOpiskeluoikeudet(
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
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.kyselyt.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[QueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
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
      val query = QueryPaallekkaisetOpiskeluoikeudet(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2000, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[QueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
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

  def addQuery[T](query: QueryParameters, user: UserWithPassword)(f: => T): T =
    post("api/kyselyt", JsonSerializer.writeWithRoot(query), headers = authHeaders(user) ++ jsonContent)(f)

  def addQuerySuccessfully[T](query: QueryParameters, user: UserWithPassword)(f: QueryResponse => T): T = {
    addQuery(query, user) {
      f(parsedResponse)
    }
  }

  def getQuery[T](queryId: String, user: UserWithPassword)(f: => T): T =
    get(s"api/kyselyt/$queryId", headers = authHeaders(user) ++ jsonContent)(f)

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
      app.kyselyScheduler.pause(Duration.ofDays(1))
      f
    } finally {
      app.kyselyScheduler.resume()
    }
}

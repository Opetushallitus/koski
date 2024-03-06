package fi.oph.koski.queuedqueries

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet.{QueryOrganisaationOpiskeluoikeudet, QueryOrganisaationOpiskeluoikeudetCsv, QueryOrganisaationOpiskeluoikeudetJson}
import fi.oph.koski.queuedqueries.paallekkaisetopiskeluoikeudet.QueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class QuerySpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  "Organisaation opiskeluoikeudet" - {
    "JSON" - {
      val query = QueryOrganisaationOpiskeluoikeudetJson(
        alkamispaiva = LocalDate.of(2020, 1, 1),
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
        waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running)
        val complete = waitForStateTransition(queryId, user)(QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

        complete.files should have length 14
        complete.files.foreach(verifyResult(_, user))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }

    "CSV" - {
      val query = QueryOrganisaationOpiskeluoikeudetCsv(
        alkamispaiva = LocalDate.of(2020, 1, 1),
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
        waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running)
        val complete = waitForStateTransition(queryId, user)(QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

        complete.files should have length 5
        complete.files.foreach(verifyResult(_, user))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
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
        waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running)
        val complete = waitForStateTransition(queryId, user)(QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))
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
        waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running)
        val complete = waitForStateTransition(queryId, user)(QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))
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

  def waitForStateTransition(queryId: String, user: UserWithPassword)(from: String, to: String): QueryResponse = {
    var lastResponse: Option[QueryResponse] = None
    Wait.until {
      getQuerySuccessfully(queryId, user) { response =>
        List(from, to) should contain(response.status)
        lastResponse = Some(response)
        response.status == to
      }
    }
    lastResponse.get
  }

  def parsedResponse: QueryResponse = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[QueryResponse](json, strictDeserialization)
    result should not be Left
    result.right.get
  }
}

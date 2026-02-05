package fi.oph.koski.massaluovutus

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods
import org.scalatest.matchers.should.Matchers

import java.net.URL
import java.time.Duration

trait MassaluovutusTestMethods extends KoskiHttpSpec with Matchers {
  val app = KoskiApplicationForTests

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

  def waitForFailure(queryId: String, user: UserWithPassword): FailedQueryResponse =
    waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running, QueryState.failed).asInstanceOf[FailedQueryResponse]

  def parsedResponse: QueryResponse = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[QueryResponse](json, strictDeserialization)
    result should not be Left
    result.toOption.get
  }

  def withoutRunningQueryScheduler[T](f: => T): T =
    try {
      app.massaluovutusScheduler.pause(Duration.ofDays(1))
      f
    } finally {
      app.massaluovutusService.truncate()
      app.massaluovutusScheduler.resume()
    }
}

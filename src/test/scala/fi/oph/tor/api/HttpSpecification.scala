package fi.oph.tor.api

import fi.oph.tor.http
import fi.oph.tor.http.{HttpStatus, BasicAuthentication, ErrorDetail}
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser
import org.json4s.JsonAST.JValue
import org.scalatest.{Assertions, Matchers}
import org.scalatra.test.HttpComponentsClient

trait HttpSpecification extends HttpComponentsClient with Assertions with Matchers {
  SharedJetty.start

  type Headers = Map[String, String]

  def authHeaders(user: MockUser = MockUsers.kalle): Headers = {
    Map(BasicAuthentication.basicAuthHeader(user.username, user.username))
  }

  val jsonContent = Map(("Content-type" -> "application/json"))

  def verifyResponseStatus(expectedStatus: Int, details: http.HttpStatus*): Unit = {
    val dets: List[ErrorDetail] = details.toList.map((status: HttpStatus) => status.errors(0))
    if (response.status != expectedStatus) {
      fail("Expected status " + expectedStatus + ", got " + response.status + ", " + response.body)
    }
    if (details.length > 0) {
      val errors: List[ErrorDetail] = Json.read[List[ErrorDetail]](body)
      if (!errors(0).message.isInstanceOf[String]) {
        // Json schema error -> just check for substring
        errors.zip(dets) foreach { case (errorDetail, expectedErrorDetail) =>
          errorDetail.key should equal(expectedErrorDetail.key)
          errorDetail.message.toString should include(expectedErrorDetail.message.toString)
        }
      } else {
        errors should equal(dets)
      }
    }
  }

  def authGet[A](uri: String, user: MockUser = MockUsers.kalle)(f: => A) = {
    get(uri, headers = authHeaders(user))(f)
  }

  def resetFixtures[A] = {
    post("fixtures/reset") {
      verifyResponseStatus(200)
    }
  }

  override def baseUrl = SharedJetty.baseUrl
}

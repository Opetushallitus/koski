package fi.oph.tor.api

import fi.oph.tor.http.BasicAuthentication
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser
import org.scalatest.{Assertions, Matchers}
import org.scalatra.test.HttpComponentsClient

trait HttpSpecification extends HttpComponentsClient with Assertions with Matchers {
  SharedJetty.start

  type Headers = Map[String, String]

  def authHeaders(user: MockUser = MockUsers.kalle): Headers = {
    Map(BasicAuthentication.basicAuthHeader(user.username, user.username))
  }

  val jsonContent = Map(("Content-type" -> "application/json"))

  def verifyResponseStatus(expectedStatus: Int = 200, expectedText: String = "") = {
    if (response.status != expectedStatus) {
      fail("Expected status " + expectedStatus + ", got " + response.status + ", " + response.body)
    }
    body should include(expectedText)
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

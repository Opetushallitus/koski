package fi.oph.tor.api

import com.unboundid.util.Base64
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser
import org.scalatest.{Matchers, Assertions}
import org.scalatra.test.HttpComponentsClient

trait HttpSpecification extends HttpComponentsClient with Assertions with Matchers {
  SharedJetty.start

  def authHeaders(user: MockUser = MockUsers.kalle): Map[String, String] = {
    val auth: String = "Basic " + Base64.encode((user.username + ":" + user.username).getBytes("UTF8"))
    Map("Authorization" -> auth)
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

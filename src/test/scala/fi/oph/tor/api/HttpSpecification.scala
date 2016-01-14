package fi.oph.tor.api

import com.unboundid.util.Base64
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser
import org.scalatest.Assertions
import org.scalatra.test.HttpComponentsClient

trait HttpSpecification extends HttpComponentsClient with Assertions {
  SharedJetty.start

  def authHeaders(user: MockUser = MockUsers.kalle): Map[String, String] = {
    val auth: String = "Basic " + Base64.encode((user.username + ":" + user.username).getBytes("UTF8"))
    Map("Authorization" -> auth)
  }

  val jsonContent = Map(("Content-type" -> "application/json"))

  def verifyResponseStatus(status: Int = 200): Unit = {
    if (response.status != status) {
      fail("Expected status " + status + ", got " + response.status + ", " + response.body)
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

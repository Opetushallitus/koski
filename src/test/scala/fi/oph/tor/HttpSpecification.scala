package fi.oph.tor

import com.unboundid.util.Base64
import fi.oph.tor.jettylauncher.SharedJetty
import org.scalatest.Assertions
import org.scalatra.test.HttpComponentsClient

trait HttpSpecification extends HttpComponentsClient with Assertions {
  val authHeaders: Map[String, String] = {
    val auth: String = "Basic " + Base64.encode("kalle:kalle".getBytes("UTF8"))
    Map("Authorization" -> auth)
  }

  val jsonContent = Map(("Content-type" -> "application/json"))

  def verifyResponseStatus(status: Int = 200): Unit = {
    if (response.status != status) {
      fail("Expected status " + status + ", got " + response.status + ", " + response.body)
    }
  }

  def authGet[A](uri: String)(f: => A) = {
    get(uri, headers = authHeaders)(f)
  }

  def resetFixtures[A](f: => A) = {
    post("fixtures/reset") {
      verifyResponseStatus(200)
      f
    }
  }

  override def baseUrl = SharedJetty.baseUrl
}

package fi.oph.tor.http

import fi.oph.tor.http
import fi.oph.tor.json.Json
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser
import org.scalatest.{Assertions, Matchers}
import org.scalatra.test.HttpComponentsClient

import scala.util.matching.Regex

trait HttpSpecification extends HttpComponentsClient with Assertions with Matchers {
  type Headers = Map[String, String]

  val jsonContent = Map(("Content-type" -> "application/json"))

  def verifyResponseStatus(expectedStatus: Int, details: http.HttpStatus*): Unit = {
    val dets: List[ErrorDetail] = details.toList.map((status: HttpStatus) => status.errors(0))
    if (response.status != expectedStatus) {
      fail("Expected status " + expectedStatus + ", got " + response.status + ", " + response.body)
    }
    if (details.length > 0) {
      val errors: List[ErrorDetail] = Json.read[List[ErrorDetail]](body)
      errors.length should equal(dets.length)
      errors.zip(dets) foreach { case (errorDetail, expectedErrorDetail) =>
        errorDetail.key should equal(expectedErrorDetail.key)
        expectedErrorDetail.message match {
          case s: String => errorDetail.message.toString should equal(s)
          case r: Regex => errorDetail.message.toString should fullyMatch regex(r)
        }
      }
    }
  }

  def resetFixtures[A] = {
    post("fixtures/reset") {
      verifyResponseStatus(200)
    }
  }
}

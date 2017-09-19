package fi.oph.koski.http

import fi.oph.koski.json.Json
import fi.oph.koski.schema.JsonSerializer
import org.json4s.JValue
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods
import org.scalatest.{Assertions, Matchers}

import scala.util.matching.Regex

trait HttpSpecification extends HttpTester with Assertions with Matchers {
  def refreshElasticSearchIndex: Unit

  def resetFixtures[A] = {
    post("fixtures/reset", Nil, authHeaders()) {
      verifyResponseStatus(200)
    }
  }

  def verifyResponseStatus(expectedStatus: Int, details: HttpStatus): Unit = {
    verifyResponseStatus(expectedStatus, FixedErrorMatcher(details.errors(0).key, details.errors(0).message))
  }

  def verifyResponseStatus(expectedStatus: Int, details: ErrorMatcher*): Unit = {
    val dets: List[ErrorMatcher] = details.toList
    if (response.status != expectedStatus) {
      fail("Expected status " + expectedStatus + ", got " + response.status + ", " + response.body)
    }
    if (details.length > 0) {
      val errors: List[ErrorDetail] = JsonSerializer.parse[List[ErrorDetail]](body)
      errors.zip(dets) foreach { case (errorDetail, expectedErrorDetail) =>
        if (errorDetail.key != expectedErrorDetail.errorKey) {
          fail("Unexpected error key " + errorDetail.key + "(expected " + expectedErrorDetail.errorKey + "), message=" + errorDetail.message)
        }
        expectedErrorDetail.matchMessage(errorDetail.message)
      }
      errors.length should equal(dets.length)
    }
  }

  import reflect.runtime.universe.TypeTag
  // TODO: validating=false in many places just because of the non-empty-string default constraint in scala-schema
  def readPaginatedResponse[T: TypeTag]: T = JsonSerializer.extract[T](Json.parse(body) \ "result", validating = false) // scala-schema doesn't support parameterized case classes like PaginatedResponse
}

object ErrorMatcher {
  def exact(errorKey: String, message: String) = FixedErrorMatcher(errorKey, JString(message))
  def exact(errorKey: ErrorCategory, message: String): FixedErrorMatcher = exact(errorKey.key, message)
  def regex(errorKey: ErrorCategory, regex: Regex) = RegexErrorMatcher(errorKey.key, regex)
}

sealed trait ErrorMatcher extends Assertions with Matchers {
  def errorKey: String
  def matchMessage(msg: JValue)
}
case class FixedErrorMatcher(errorKey: String, message: JValue) extends ErrorMatcher {
  override def matchMessage(msg: JValue): Unit = msg should equal(message)
}
case class RegexErrorMatcher(errorKey: String, pattern: Regex) extends ErrorMatcher {
  override def matchMessage(msg: JValue): Unit = msg match {
    case JString(msg) => msg should fullyMatch regex(pattern)
    case _ => JsonMethods.compact(msg) should fullyMatch regex(pattern)
  }
}

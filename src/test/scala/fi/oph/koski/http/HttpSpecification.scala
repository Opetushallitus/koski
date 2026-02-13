package fi.oph.koski.http

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s.JValue
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.Assertions
import org.scalatest.matchers.should.Matchers

import scala.util.matching.Regex

trait HttpSpecification extends HttpTester with TestEnvironment with Assertions with Matchers {
  def resetFixtures(): Unit = {
    post("fixtures/reset", Nil, authHeaders()) {
      verifyResponseStatus(200, Nil)
    }
  }

  def resetFixturesAfter(f: => Unit): Unit = {
    try {
      f
    } finally {
      resetFixtures()
    }
  }

  def resetFixturesSkipInvalidOpiskeluoikeudet(): Unit = {
    post("fixtures/reset", Seq(("skipInvalidOpiskeluoikeudet", "true")), authHeaders()) {
      verifyResponseStatus(200, Nil)
    }
  }

  def clearOppijanOpiskeluoikeudet(oppijaOid: String): Unit = {
    post("fixtures/clear-oppijan-opiskeluoikeudet", Seq(("oppija_oid", oppijaOid)), authHeaders()) {
      verifyResponseStatus(200, Nil)
    }
  }

  def verifyResponseStatusOk(expectedStatus: Int = 200): Unit = {
    verifyResponseStatus(expectedStatus, Nil)
  }

  def verifyResponseStatus(expectedStatus: Int, details: HttpStatus): Unit = {
    verifyResponseStatus(expectedStatus, details.errors.map { case ErrorDetail(key, message) => FixedErrorMatcher(key, message) })
  }

  def verifyResponseStatus(expectedStatus: Int, details: ErrorMatcher): Unit = {
    verifyResponseStatus(expectedStatus, List(details))
  }

  def verifyResponseStatus(expectedStatus: Int, details: HttpStatus*): Unit = {
    verifyResponseStatus(expectedStatus, details.flatMap { _.errors }.map { case ErrorDetail(key, message) => FixedErrorMatcher(key, message) }.toList)
  }

  def verifyResponseStatus(expectedStatus: Int, details: List[ErrorMatcher]): Unit = {
    if (response.status != expectedStatus) {
      fail("Expected status " + expectedStatus + ", got " + response.status + ", " + response.body)
    }
    if (details.length > 0) {
      val errors: List[ErrorDetail] = JsonSerializer.parse[List[ErrorDetail]](body)
      errors.zip(details) foreach { case (errorDetail, expectedErrorDetail) =>
        if (errorDetail.key != expectedErrorDetail.errorKey) {
          fail("Unexpected error key " + errorDetail.key + "(expected " + expectedErrorDetail.errorKey + "), message=" + JsonMethods.pretty(errorDetail.message))
        }
        expectedErrorDetail.matchMessage(errorDetail.message)
      }
      errors.length should equal(details.length)
    }
  }

  def kansalainenLoginHeaders(hetu: String): List[(String, String)] = {
    get("cas/oppija", headers = List("hetu" -> hetu, "security" -> "mock")) {
      verifyResponseStatusOk(302)
      val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiOppija")).get
      List("Cookie" -> cookie)
    }
  }

  import reflect.runtime.universe.TypeTag

  def readValidatedResponse[T: TypeTag]: T = {
    val result = JsonMethods.parse(response.getContent())
    KoskiApplicationForTests
      .validatingAndResolvingExtractor.extract[T](strictDeserialization)(result)
      .fold({ err => fail(err.errorString.getOrElse(err.toString)) }, identity)
  }

  def readPaginatedResponse[T: TypeTag]: T = try {
    JsonSerializer.extract[T](parse(body) \ "result")
  } catch {
    case e: Exception =>
      println(body)
      throw e
  }
}

object ErrorMatcher {
  def exact(errorKey: String, message: String) = FixedErrorMatcher(errorKey, JString(message))
  def exact(errorKey: ErrorCategory, message: String): FixedErrorMatcher = exact(errorKey.key, message)
  def regex(errorKey: ErrorCategory, regex: Regex) = RegexErrorMatcher(errorKey.key, regex)
}

sealed trait ErrorMatcher extends Assertions with Matchers {
  def errorKey: String
  def matchMessage(msg: JValue): Unit
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

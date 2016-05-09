package fi.oph.tor.log

import java.lang.reflect.{InvocationTargetException, UndeclaredThrowableException}
import java.util.concurrent.ExecutionException

import fi.oph.tor.http.{Http, HttpStatusException}
import Http._
import org.http4s.Request
import org.scalatest.{FreeSpec, Matchers}

class ExceptionLoggingSpec extends FreeSpec with Matchers {
  val renderer: LoggableThrowableRenderer = new LoggableThrowableRenderer()

  "Loggable exceptions" - {
    val httpException = new HttpStatusException(500, "Server error", Request(uri = uri"/test"))
    val exceptionText = "fi.oph.tor.http.HttpStatusException: 500: Server error when requesting GET /test"

    "Stack traces are hidden" in {
      val result: Array[String] = renderer.doRender(httpException)
      result.length should equal(1)
    }

    "Stack traces hidden even when nested in wrapping exceptions" in {
      val deeplyNestedException = new UndeclaredThrowableException(new ExecutionException(new InvocationTargetException(httpException)))
      val result: Array[String] = renderer.doRender(deeplyNestedException)
      result.length should equal(1)
      result should equal(Array(exceptionText))
    }
  }

  "Other exceptions" - {
    "Stack traces are included" in {
      renderer.doRender(new RuntimeException()).length should be > 10
    }
  }
}

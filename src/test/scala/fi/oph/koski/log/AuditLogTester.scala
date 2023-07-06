package fi.oph.koski.log

import fi.oph.koski.json.GenericJsonFormats
import fi.oph.koski.log.AuditLogTester.retryingTest
import org.json4s.Formats
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.matchers.should.Matchers

import scala.annotation.tailrec

object AuditLogTester extends Matchers with LogTester {
  override def appenderName: String = "Audit"

  def verifyAuditLogMessage(params: Map[String, Any]): Unit = {
    retryingTest(times = 10) { () =>
      val message = getLogMessages.lastOption.map(m => parse(m))
      message match {
        case Some(msg: JObject) => verifyAuditLogObject(msg, params)
        case _ => throw new IllegalStateException("No audit log message found")
      }
    }
  }

  def verifyAuditLogString(loggingEvent: String, params: Map[String, Any]): Unit =
    parse(loggingEvent) match {
      case msg: JObject => verifyAuditLogObject(msg, params)
      case _ => throw new IllegalStateException("No audit log message found")
    }

  def verifyNoAuditLogMessages(): Unit =
    if (getLogMessages.nonEmpty) {
      throw new IllegalStateException("Audit log message found, expected none")
    }

  private def verifyAuditLogObject(msg: JObject, params: Map[String, Any]): Unit = {
    implicit val formats: Formats = GenericJsonFormats.genericFormats
    withClue(s"Audit log message: $msg | Expected: $params") {
      params.foreach {
        case (key, expectedValue: String) =>
          msg.values.get(key) should equal(Some(expectedValue))
        case (key, newParams: Map[String, Any]@unchecked) =>
          verifyAuditLogObject((msg \ key).extract[JObject], newParams)
        case _ => ???
      }
    }
  }

  private def retryingTest(times: Int)(fn: () => Unit): Unit = {
    try {
      fn()
    } catch {
      case _: Throwable if times > 1 => {
        Thread.sleep(1000)
        retryingTest(times - 1)(fn)
      }
      case t: Throwable => throw t
    }
  }
}

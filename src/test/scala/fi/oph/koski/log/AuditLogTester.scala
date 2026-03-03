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

  def verifyOnlyAuditLogMessageForOperation(params: Map[String, Any]): Unit = {
    retryingTest(times = 10) { () =>
      filteredMessagesByOperation(params) should have length(1)
      verifyLastMatchingAuditLogMessageContains(params)
    }
  }

  def verifyLastAuditLogMessageForOperation(params: Map[String, Any]): Unit = {
    retryingTest(times = 10) { () =>
      verifyLastMatchingAuditLogMessageContains(params)
    }
  }

  private def filteredMessagesByOperation(params: Map[String, Any]): Seq[JObject] = {
    implicit val formats: Formats = GenericJsonFormats.genericFormats
    val messages = getLogMessages
      .map(m => parse(m))
      .collect { case msg: JObject if !isAliveMessage(msg) => msg }

    params.get("operation") match {
      case Some(expectedOp: String) =>
        messages.filter(msg => msg.values.get("operation").contains(expectedOp))
      case _ =>
        messages
    }
  }

  private def verifyLastMatchingAuditLogMessageContains(params: Map[String, Any]): Unit = {
    filteredMessagesByOperation(params).lastOption match {
      case Some(msg: JObject) => verifyAuditLogObject(msg, params)
      case _ => throw new IllegalStateException(s"No audit log message found matching $params")
    }
  }

  private def isAliveMessage(msg: JObject): Boolean = {
    implicit val formats: Formats = GenericJsonFormats.genericFormats
    msg.values.get("type").contains("alive")
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

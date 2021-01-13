package fi.oph.common.log

import fi.oph.common.json.GenericJsonFormats
import fi.vm.sade.auditlog.Audit
import org.apache.log4j.Logger
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.Matchers
object AuditLogTester extends Matchers with LogTester {
  def verifyAuditLogMessage(params: Map[String, Any]): Unit = {
    val message = getLogMessages.lastOption.map(m => parse(m.getMessage.toString))
    message match {
      case Some(msg: JObject) => verifyAuditLogMessage(msg, params)
      case _ => throw new IllegalStateException("No audit log message found")
    }
  }

  private def verifyAuditLogMessage(msg: JObject, params: Map[String, Any]): Unit = {
    implicit val formats = GenericJsonFormats.genericFormats
    params.foreach {
      case (key, expectedValue: String) =>
        msg.values.get(key) should equal(Some(expectedValue))
      case (key, newParams: Map[String, Any] @unchecked) =>
        verifyAuditLogMessage((msg \ key).extract[JObject], newParams)
      case _ => ???
    }
  }

  override def getLogger: Logger = Logger.getLogger(classOf[Audit])
}

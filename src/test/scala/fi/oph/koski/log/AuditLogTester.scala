package fi.oph.koski.log

import fi.oph.koski.json.GenericJsonFormats
import fi.vm.sade.auditlog.Audit
import org.apache.log4j.Logger
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.Matchers
object AuditLogTester extends Matchers with LogTester {
  def verifyAuditLogMessage(params: Map[String, Any]): Unit = {
    val message = getLogMessages.lastOption.map(m => parse(m.getMessage.toString))
    message match {
      case None => throw new IllegalStateException("No audit log message found")
      case Some(msg: JObject) => verifyAuditLogMessage(msg, params)
    }
  }

  private def verifyAuditLogMessage(msg: JObject, params: Map[String, Any]): Unit = {
    implicit val formats = GenericJsonFormats.genericFormats
    params.foreach {
      case (key, expectedValue: String) =>
        msg.values.get(key) should equal(Some(expectedValue))
      case (key, newParams: Map[String, Any]) =>
        verifyAuditLogMessage((msg \ key).extract[JObject], newParams)
    }
  }

  override def getLogger: Logger = Logger.getLogger(classOf[Audit])
}

package fi.oph.koski.log

import fi.oph.koski.json.Json
import fi.vm.sade.auditlog.Audit
import org.apache.log4j.Logger
import org.json4s.JsonAST.JObject
import org.scalatest.Matchers

object AuditLogTester extends Matchers with LogTester {
  def verifyAuditLogMessage(params: Map[String, String]): Unit = {
    val message = getLogMessages.lastOption.map(m => Json.read[JObject](m.getMessage.toString))
    message match {
      case None => throw new IllegalStateException("No audit log message found")
      case Some(msg) =>
        params.toList.foreach {
          case (key, expectedValue) =>
            msg.values.get(key) should equal(Some(expectedValue))
        }
    }
  }

  override def getLogger: Logger = Logger.getLogger(classOf[Audit])
}

package fi.oph.koski.log

import fi.oph.koski.json.Json
import fi.vm.sade.auditlog.Audit
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, Logger}
import org.json4s.JsonAST.{JString, JObject}
import org.scalatest.Matchers

object AuditLogTester extends Matchers {
  private var messages: List[JObject] = Nil

  lazy val setup: Unit = {
    Logger.getLogger(classOf[Audit]).addAppender(new AppenderSkeleton() {
      override def append(event: LoggingEvent) = AuditLogTester.synchronized {
        val message:JObject = Json.read[JObject](event.getMessage.toString)
        messages ++= List(message)
      }

      override def requiresLayout() = false

      override def close() {}
    })
  }

  def verifyAuditLogMessage(params: Map[String, String]): Unit = {
    val message = this.synchronized { messages.lastOption }
    message match {
      case None => throw new IllegalStateException("No audit log message found")
      case Some(msg) =>
        params.toList.foreach {
          case (key, expectedValue) =>
            msg.values.get(key) should equal(Some(expectedValue))
        }
    }
  }
}

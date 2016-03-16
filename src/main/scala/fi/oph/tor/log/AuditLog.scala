package fi.oph.tor.log

import fi.oph.tor.log.TorOperation.TorOperation
import fi.oph.tor.toruser.TorUser
import fi.vm.sade.auditlog._
import org.slf4j.{LoggerFactory, Logger}

object AuditLog extends AuditLog(LoggerFactory.getLogger(classOf[Audit].getName)){
}

class AuditLog(logger: Logger) {
  val audit = new Audit(logger, "koski", ApplicationType.BACKEND)

  def log(msg: AuditLogMessage): Unit = {
    audit.log(new TorLogMessageBuilder(msg).build)
  }

  private class TorLogMessageBuilder(msg: AuditLogMessage) extends SimpleLogMessageBuilder[TorLogMessageBuilder] {
    def build = new AbstractLogMessage(mapping) {
      safePut(CommonLogMessageFields.OPERAATIO, msg.operation.toString)
      safePut(TorMessageField.CLIENT_IP.toString, msg.user.clientIp)
    }
  }
}

case class AuditLogMessage(operation: TorOperation, user: TorUser)

object TorOperation extends Enumeration {
  type TorOperation = Value
  val OPISKELUOIKEUS_LISAYS = Value
  val OPISKELUOIKEUS_KATSOMINEN = Value
}

object TorMessageField extends Enumeration {
  type TorMessageField = Value
  val CLIENT_IP = Value
}
package fi.oph.tor.log

import fi.oph.tor.log.TorMessageField.TorMessageField
import fi.oph.tor.log.TorOperation.TorOperation
import fi.oph.tor.toruser.TorUser
import fi.vm.sade.auditlog._
import org.slf4j.{Logger, LoggerFactory}

object AuditLog extends AuditLog(LoggerFactory.getLogger(classOf[Audit].getName)){
}

class AuditLog(logger: Logger) {
  val audit = new Audit(logger, "koski", ApplicationType.BACKEND)

  def log(msg: AuditLogMessage): Unit = {
    audit.log(new TorLogMessageBuilder(msg).build)
  }

  private class TorLogMessageBuilder(msg: AuditLogMessage) extends SimpleLogMessageBuilder[TorLogMessageBuilder] {
    def build = new AbstractLogMessage(mapping) {
      safePut(TorMessageField.clientIp.toString, msg.user.clientIp)
      safePut(CommonLogMessageFields.OPERAATIO, msg.operation.toString)
      safePut(TorMessageField.kayttajaHenkiloOid.toString, msg.user.oid)

      msg.extraFields.toList.foreach { case (k: TorMessageField,v: String) =>
        safePut(k.toString, v)
      }
    }
  }
}

case class AuditLogMessage(operation: TorOperation, user: TorUser, extraFields: Map[TorMessageField, String])

object TorMessageField extends Enumeration {

  type TorMessageField = Value
  val clientIp, oppijaHenkiloOid, kayttajaHenkiloOid, opiskeluOikeusId, opiskeluOikeusVersio, oppijaHakuEhto = Value
}

object TorOperation extends Enumeration {
  type TorOperation = Value
  val LOGIN, OPISKELUOIKEUS_LISAYS, OPISKELUOIKEUS_MUUTOS, OPISKELUOIKEUS_KATSOMINEN, MUUTOSHISTORIA_KATSOMINEN, OPPIJA_HAKU = Value
}


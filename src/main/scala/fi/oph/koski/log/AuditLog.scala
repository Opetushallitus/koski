package fi.oph.koski.log

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.KoskiMessageField.KoskiMessageField
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.vm.sade.auditlog._
import org.slf4j.{Logger, LoggerFactory}

object AuditLog extends AuditLog(LoggerFactory.getLogger(classOf[Audit].getName))

class AuditLog(logger: Logger) {
  val audit = new Audit(logger, "koski", ApplicationType.BACKEND)

  def log(msg: AuditLogMessage): Unit = {
    audit.log(new KoskiLogMessageBuilder(msg).build)
  }

  private class KoskiLogMessageBuilder(msg: AuditLogMessage) extends SimpleLogMessageBuilder[KoskiLogMessageBuilder] {
    def build = new AbstractLogMessage(mapping) {
      safePut(KoskiMessageField.clientIp.toString, msg.user.clientIp)
      safePut(CommonLogMessageFields.OPERAATIO, msg.operation.toString)
      safePut(KoskiMessageField.kayttajaHenkiloOid.toString, msg.user.oid)

      msg.extraFields.toList.foreach { case (k: KoskiMessageField,v: String) =>
        safePut(k.toString, v)
      }
    }
  }
}

case class AuditLogMessage(operation: KoskiOperation, user: KoskiUser, extraFields: Map[KoskiMessageField, String])

object KoskiMessageField extends Enumeration {

  type KoskiMessageField = Value
  val clientIp, oppijaHenkiloOid, kayttajaHenkiloOid, opiskeluOikeusId, opiskeluOikeusVersio, hakuEhto, juuriOrganisaatio = Value
}

object KoskiOperation extends Enumeration {
  type KoskiOperation = Value
  val LOGIN, OPISKELUOIKEUS_LISAYS, OPISKELUOIKEUS_MUUTOS, OPISKELUOIKEUS_KATSOMINEN, OPISKELUOIKEUS_HAKU, MUUTOSHISTORIA_KATSOMINEN, OPPIJA_HAKU, TIEDONSIIRTO_KATSOMINEN = Value
}


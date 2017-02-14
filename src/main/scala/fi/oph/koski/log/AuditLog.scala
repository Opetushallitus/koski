package fi.oph.koski.log

import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSession, UserWithOid}
import fi.oph.koski.log.KoskiMessageField.KoskiMessageField
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.vm.sade.auditlog._
import io.prometheus.client.Counter
import org.slf4j.{Logger, LoggerFactory}

object AuditLog extends AuditLog(LoggerFactory.getLogger(classOf[Audit].getName))

class AuditLog(logger: Logger) {
  private val audit = new Audit(logger, "koski", ApplicationType.BACKEND)
  private val counter = Counter.build().name("fi_oph_koski_log_AuditLog").help("Koski audit log events").labelNames("operation").register()

  def log(msg: AuditLogMessage): Unit = {
    audit.log(new KoskiLogMessageBuilder(msg).build)
    counter.labels(msg.operation.toString).inc
  }

  private class KoskiLogMessageBuilder(msg: AuditLogMessage) extends SimpleLogMessageBuilder[KoskiLogMessageBuilder] {
    def build = new AbstractLogMessage(mapping) {
      safePut(KoskiMessageField.clientIp.toString, msg.clientIp)
      safePut(CommonLogMessageFields.OPERAATIO, msg.operation.toString)
      safePut(KoskiMessageField.kayttajaHenkiloOid.toString, msg.user.oid)
      safePut(KoskiMessageField.kayttajaHenkiloNimi.toString, msg.user.name)

      msg.extraFields.toList.foreach { case (k: KoskiMessageField,v: String) =>
        safePut(k.toString, v)
      }
    }
  }
}

case class AuditLogMessage(operation: KoskiOperation, user: AuthenticationUser, clientIp: String, extraFields: Map[KoskiMessageField, String])

object AuditLogMessage {
  def apply(operation: KoskiOperation, session: KoskiSession, extraFields: Map[KoskiMessageField, String]): AuditLogMessage =
    AuditLogMessage(operation, session.user, session.clientIp, extraFields)
}

object KoskiMessageField extends Enumeration {
  type KoskiMessageField = Value
  val clientIp, oppijaHenkiloOid, kayttajaHenkiloOid, kayttajaHenkiloNimi, opiskeluoikeusId, opiskeluoikeusVersio, hakuEhto, juuriOrganisaatio = Value
}

object KoskiOperation extends Enumeration {
  type KoskiOperation = Value
  val LOGIN, OPISKELUOIKEUS_LISAYS, OPISKELUOIKEUS_MUUTOS, OPISKELUOIKEUS_KATSOMINEN, OPISKELUOIKEUS_HAKU, MUUTOSHISTORIA_KATSOMINEN, OPPIJA_HAKU, TIEDONSIIRTO_KATSOMINEN = Value
}


package fi.oph.koski.log

import java.net.InetAddress

import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSession}
import fi.oph.koski.log.KoskiMessageField.KoskiMessageField
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.vm.sade.auditlog._
import io.prometheus.client.Counter
import org.ietf.jgss.Oid
import org.slf4j.{Logger => SLogger, LoggerFactory}

class AuditLogger(logger: SLogger) extends Logger {
  override def log(msg: String): Unit = logger.info(msg)
}

object AuditLog extends AuditLog(new AuditLogger(LoggerFactory.getLogger(classOf[Audit].getName)))

class AuditLog(logger: Logger) {
  private val audit = new Audit(logger, "koski", ApplicationType.BACKEND)
  private val counter = Counter.build().name("fi_oph_koski_log_AuditLog").help("Koski audit log events").labelNames("operation").register()

  def log(msg: AuditLogMessage): Unit = {
    audit.log(msg.user, msg.operation, msg.target, msg.changes)
    counter.labels(msg.operation.toString).inc
  }
}

case class AuditLogMessage(user: User, operation: Operation, target: Target, changes: Changes)

object AuditLogMessage {
  def apply(operation: KoskiOperation, user: AuthenticationUser, clientIp: InetAddress, serviceTicket: String, userAgent: String): AuditLogMessage = {
    build(operation, new User(new Oid(user.oid), clientIp, serviceTicket, userAgent), Map())
  }

  def apply(operation: KoskiOperation, session: KoskiSession, extraFields: Map[KoskiMessageField, String]): AuditLogMessage = {
    val user = if (session.user.isSuoritusjakoKatsominen) {
      new User(session.clientIp, "", session.userAgent)
    } else {
      new User(new Oid(session.user.oid), session.clientIp, session.user.serviceTicket.getOrElse(""), session.userAgent)
    }
    build(operation, user, extraFields)
  }

  private def build(operation: KoskiOperation, user: User, extraFields: Map[KoskiMessageField, String]): AuditLogMessage = {
    val target = extraFields.foldLeft(new Target.Builder()) { case (builder, (name, value)) =>
      builder.setField(name.toString, value)
    }.build
    AuditLogMessage(user = user, operation = new AuditLogOperation(operation), target = target, changes = new Changes.Builder().build)
  }
}

object KoskiMessageField extends Enumeration {
  type KoskiMessageField = Value
  val clientIp, oppijaHenkiloOid, kayttajaHenkiloOid, kayttajaHenkiloNimi, opiskeluoikeusOid, opiskeluoikeusId, opiskeluoikeusVersio, hakuEhto, juuriOrganisaatio = Value
}

object KoskiOperation extends Enumeration {
  type KoskiOperation = Value
  val LOGIN, OPISKELUOIKEUS_LISAYS, OPISKELUOIKEUS_MUUTOS, OPISKELUOIKEUS_KATSOMINEN, OPISKELUOIKEUS_HAKU, MUUTOSHISTORIA_KATSOMINEN, OPPIJA_HAKU, TIEDONSIIRTO_KATSOMINEN,
      KANSALAINEN_LOGIN, KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN, KANSALAINEN_SUORITUSJAKO_LISAYS, KANSALAINEN_SUORITUSJAKO_KATSOMINEN = Value
}

class AuditLogOperation(op: KoskiOperation) extends Operation {
  override def name(): String = op.toString
  override def toString: String = op.toString
}


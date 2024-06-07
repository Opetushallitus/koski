package fi.oph.koski.log

import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, Session}
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.vm.sade.auditlog._
import io.prometheus.client.Counter
import org.ietf.jgss.Oid
import org.slf4j.{LoggerFactory, Logger => SLogger}

import java.net.InetAddress

class AuditLogger(logger: SLogger) extends Logger {
  override def log(msg: String): Unit = {
    if (msg.length > LogConfiguration.logMessageMaxLength) {
      throw new RuntimeException(s"Audit log message payload exceeds ${LogConfiguration.logMessageMaxLength} characters")
    }

    logger.info(msg)
  }
}

object AuditLog extends AuditLog(new AuditLogger(LoggerFactory.getLogger(classOf[Audit].getName))) {
  private val counter = Counter.build().name("fi_oph_koski_log_AuditLog").help("Koski audit log events").labelNames("operation").register()
}

class AuditLog(logger: Logger) {
  private val audit = new Audit(logger, "koski", ApplicationType.BACKEND)

  def log(msg: AuditLogMessage): Unit = {
    audit.log(msg.user, msg.operation, msg.target, msg.changes)
    AuditLog.counter.labels(msg.operation.toString).inc()
  }

  def startHeartbeat(): Unit = {
    // no need to do anything here, calling AuditLogger constructor is enough
  }
}

case class AuditLogMessage(user: User, operation: Operation, target: Target, changes: Changes)

object AuditLogMessage {
  type AuditLogMessageField = Enumeration#Value
  type ExtraFields = Map[AuditLogMessageField, String]

  def apply(operation: AuditLogOperation, user: User, extraFields: ExtraFields): AuditLogMessage = {
    val target = extraFields.foldLeft(new Target.Builder()) { case (builder, (name, value)) =>
      builder.setField(name.toString, value)
    }.build
    AuditLogMessage(
      user = user,
      operation = operation,
      target = target,
      changes = new Changes.Builder().build
    )
  }

  def apply(operation: AuditLogOperation, session: Session, extraFields: ExtraFields): AuditLogMessage = {
    val user = new User(
      new Oid(session.user.oid),
      session.clientIp,
      session.user.serviceTicket.getOrElse(""),
      session.userAgent
    )
    apply(operation, user, extraFields)
  }
}

object KoskiAuditLogMessage {
  def apply(operation: KoskiOperation, user: AuthenticationUser, clientIp: InetAddress, serviceTicket: String, userAgent: String): AuditLogMessage = {
    val extraFields: AuditLogMessage.ExtraFields = Map()
    AuditLogMessage(
      new KoskiAuditLogOperation(operation),
      new User(new Oid(user.oid), clientIp, serviceTicket, userAgent),
      extraFields
    )
  }

  def apply(operation: KoskiOperation, session: KoskiSpecificSession, extraFields: AuditLogMessage.ExtraFields): AuditLogMessage = {
    val logOp = new KoskiAuditLogOperation(operation)
    if (session.user.isSuoritusjakoKatsominen || session.user.isYtrDownloadUser) {
      val user = new User(session.clientIp, "", session.userAgent)
      AuditLogMessage(logOp, user, extraFields)
    } else {
      AuditLogMessage(logOp, session, extraFields)
    }
  }
}

object KoskiAuditLogMessageField extends Enumeration {
  type KoskiAuditLogMessageField = Value
  val clientIp,
  oppijaHenkiloOid,
  kayttajaHenkiloOid,
  kayttajaHenkiloNimi,
  opiskeluoikeusOid,
  opiskeluoikeusId,
  opiskeluoikeusVersio,
  hakuEhto,
  juuriOrganisaatio,
  omaDataKumppani,
  suorituksenTyyppi = Value
}

object KoskiOperation extends Enumeration {
  type KoskiOperation = Value
  val LOGIN,
  OPISKELUOIKEUS_LISAYS,
  OPISKELUOIKEUS_MUUTOS,
  OPISKELUOIKEUS_KATSOMINEN,
  OPISKELUOIKEUS_HAKU,
  OPISKELUOIKEUS_RAPORTTI,
  MUUTOSHISTORIA_KATSOMINEN,
  OPPIJA_HAKU,
  TIEDONSIIRTO_KATSOMINEN,
  KANSALAINEN_LOGIN,
  KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN,
  KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN,
  KANSALAINEN_YLIOPPILASKOE_HAKU,
  KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU,
  KANSALAINEN_SUORITUSJAKO_LISAYS,
  KANSALAINEN_SUORITUSJAKO_LISAYS_SUORITETUT_TUTKINNOT,
  KANSALAINEN_SUORITUSJAKO_LISAYS_AKTIIVISET_JA_PAATTYNEET_OPINNOT,
  KANSALAINEN_SUORITUSJAKO_KATSOMINEN,
  KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT,
  KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT,
  KANSALAINEN_MYDATA_LISAYS,
  KANSALAINEN_MYDATA_POISTO,
  KANSALAINEN_SUOMIFI_KATSOMINEN,
  KANSALAINEN_SUORITUSJAKO_TEKEMÄTTÄ_KATSOMINEN,
  KANSALAINEN_SUOSTUMUS_PERUMINEN,
  YTR_OPISKELUOIKEUS_LISAYS,
  YTR_OPISKELUOIKEUS_MUUTOS,
  YTR_OPISKELUOIKEUS_KATSOMINEN,
  YTR_YOTODISTUKSEN_LUONTI,
  YTR_YOTODISTUKSEN_LATAAMINEN,
  VALINTAPALVELU_OPISKELUOIKEUS_HAKU,
  SUORITUSREKISTERI_OPISKELUOIKEUS_HAKU = Value
}

private class KoskiAuditLogOperation(op: KoskiOperation) extends AuditLogOperation(op)

class AuditLogOperation(op: Enumeration#Value) extends Operation {
  override def name(): String = op.toString

  override def toString: String = op.toString
}


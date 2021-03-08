package fi.oph.koski.valpas.log

import fi.oph.koski.log.AuditLogMessage
import fi.oph.koski.log.KoskiMessageField.KoskiMessageField
import fi.oph.koski.valpas.log.ValpasOperation.ValpasOperation
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.vm.sade.auditlog.{Changes, Operation, Target, User}
import org.ietf.jgss.Oid

object ValpasAuditLogMessage {
  def apply(
    operation: ValpasOperation,
    extraFields: Map[KoskiMessageField, String]
  )(implicit session: ValpasSession): AuditLogMessage = {
    val target = extraFields.foldLeft(new Target.Builder()) { case (builder, (name, value)) =>
      builder.setField(name.toString, value)
    }.build
    AuditLogMessage(
      user = new User(
        new Oid(session.user.oid),
        session.clientIp,
        session.user.serviceTicket.getOrElse(""),
        session.userAgent),
      operation = new ValpasAuditLogOperation(operation),
      target = target,
      changes = new Changes.Builder().build
    )
  }
}

object ValpasOperation extends Enumeration {
  type ValpasOperation = Value
  val VALPAS_OPPIJA_KATSOMINEN,
      VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN = Value
}

class ValpasAuditLogOperation(op: ValpasOperation) extends Operation {
  override def name(): String = op.toString
  override def toString: String = op.toString
}

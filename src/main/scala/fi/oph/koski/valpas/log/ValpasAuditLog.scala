package fi.oph.koski.valpas.log

import fi.oph.koski.log.KoskiMessageField.KoskiMessageField
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiMessageField}
import fi.oph.koski.valpas.log.ValpasOperation.ValpasOperation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppilaitos}
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedotJaOppijaOid
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.vm.sade.auditlog.{Changes, Operation, Target, User}
import org.ietf.jgss.Oid

object ValpasAuditLog {
  def auditLogOppijaKatsominen(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Unit =
    AuditLog.log(makeMessage(
      session,
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      Map(KoskiMessageField.oppijaHenkiloOid -> oppijaOid)
    ))

  def auditLogOppilaitosKatsominen
    (oppilaitosOid: ValpasOppilaitos.Oid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(makeMessage(
      session,
      ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      Map(KoskiMessageField.juuriOrganisaatio -> oppilaitosOid)
    ))
  }

  def auditLogOppijaKuntailmoitus
    (ilmoitus: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(makeMessage(
      session,
      ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS,
      // TODO: pitäisikö olla muutakin dataa kuin oppijan oid? Ts. pitäisikö auditlogista näkyä,
      //  että mikä oppilaitos/kunta on tehnyt ilmoituksen mihin kuntaan?
      Map(KoskiMessageField.oppijaHenkiloOid -> ilmoitus.oppijaOid)
    ))
  }

  private def makeMessage(session: ValpasSession, operation: ValpasOperation, extraFields: Map[KoskiMessageField, String]): AuditLogMessage = {
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
      VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      VALPAS_OPPIJA_KUNTAILMOITUS = Value
}

private class ValpasAuditLogOperation(op: ValpasOperation) extends Operation {
  override def name(): String = op.toString
  override def toString: String = op.toString
}

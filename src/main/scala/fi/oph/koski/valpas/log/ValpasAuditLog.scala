package fi.oph.koski.valpas.log

import fi.oph.koski.log.{AuditLog, AuditLogMessage, AuditLogOperation}
import fi.oph.koski.valpas.log.ValpasOperation.ValpasOperation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppilaitos}
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedotJaOppijaOid
import fi.oph.koski.valpas.valpasuser.ValpasSession

object ValpasAuditLog {
  def auditLogOppijaKatsominen(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Unit =
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.oppijaHenkiloOid -> oppijaOid)
    ))

  def auditLogOppilaitosKatsominen
    (oppilaitosOid: ValpasOppilaitos.Oid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.juuriOrganisaatio -> oppilaitosOid)
    ))
  }

  def auditLogOppijaKuntailmoitus
    (ilmoitus: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS,
      session,
      // TODO: pitäisikö olla muutakin dataa kuin oppijan oid? Ts. pitäisikö auditlogista näkyä,
      //  että mikä oppilaitos/kunta on tehnyt ilmoituksen mihin kuntaan?
      Map(ValpasAuditLogMessageField.oppijaHenkiloOid -> ilmoitus.oppijaOid)
    ))
  }
}

object ValpasAuditLogMessage {
  def apply(operation: ValpasOperation, session: ValpasSession, extraFields: AuditLogMessage.ExtraFields): AuditLogMessage = {
    AuditLogMessage(new ValpasAuditLogOperation(operation), session, extraFields)
  }
}

object ValpasAuditLogMessageField extends Enumeration {
  type ValpasAuditLogMessageField = Value
  val oppijaHenkiloOid, juuriOrganisaatio = Value
}

object ValpasOperation extends Enumeration {
  type ValpasOperation = Value
  val VALPAS_OPPIJA_KATSOMINEN,
      VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      VALPAS_OPPIJA_KUNTAILMOITUS = Value
}

private class ValpasAuditLogOperation(op: ValpasOperation) extends AuditLogOperation(op)

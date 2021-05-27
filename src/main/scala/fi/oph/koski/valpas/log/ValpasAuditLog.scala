package fi.oph.koski.valpas.log

import fi.oph.koski.log.{AuditLog, AuditLogMessage, AuditLogOperation}
import fi.oph.koski.valpas.ValpasHenkilöHakutiedot
import fi.oph.koski.valpas.log.ValpasOperation.ValpasOperation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOppilaitos}
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedotJaOppijaOid
import fi.oph.koski.valpas.valpasuser.ValpasSession

object ValpasAuditLog {
  def auditLogOppijaKatsominen(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Unit =
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.oppijaHenkilöOid -> oppijaOid)
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
      Map(
        ValpasAuditLogMessageField.oppijaHenkilöOid -> ilmoitus.oppijaOid,
        ValpasAuditLogMessageField.ilmoittajaHenkilöOid -> ilmoitus.kuntailmoitus.tekijä.henkilö.map(_.oid.toString).getOrElse(""),
        ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid -> ilmoitus.kuntailmoitus.tekijä.organisaatio.oid,
        ValpasAuditLogMessageField.kohdeOrganisaatioOid -> ilmoitus.kuntailmoitus.kunta.oid
      )
    ))
  }

  def auditLogHenkilöHaku
    (henkilö: ValpasHenkilöHakutiedot)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_HAKU,
      session,
      Map(
        ValpasAuditLogMessageField.oppijaHenkilöOid -> henkilö.oid,
      )
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
  val oppijaHenkilöOid,
      juuriOrganisaatio,
      ilmoittajaHenkilöOid,
      ilmoittajaOrganisaatioOid,
      kohdeOrganisaatioOid = Value
}

object ValpasOperation extends Enumeration {
  type ValpasOperation = Value
  val VALPAS_OPPIJA_KATSOMINEN,
      VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      VALPAS_OPPIJA_KUNTAILMOITUS,
      VALPAS_OPPIJA_HAKU = Value
}

private class ValpasAuditLogOperation(op: ValpasOperation) extends AuditLogOperation(op)

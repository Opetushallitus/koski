package fi.oph.koski.log

import fi.oph.koski.koskiuser.Session
import fi.oph.koski.log.AuditLogMessage.AuditLogMessageField
import fi.oph.koski.log.KoskiOperation.KoskiOperation

object OpiskeluoikeusOiditAuditLog {
  // Yhden oppijan opiskeluoikeus-oidit voi olla niin monta (esim. runsaasti linkitetyt henkilöt), että ne eivät
  // mahtuisi yhteen audit-lokiviestiin ilman että LogConfiguration.logMessageMaxLength (5000 merkkiä) ylittyy ja
  // AuditLogger heittää poikkeuksen (joka kaataisi koko massaluovutuksen). Pilkotaan oidit korkeintaan tämän
  // kokoisiin ryhmiin, jolloin yksi viesti pysyy reilusti rajan alla (100 oidia ~2800 merkkiä).
  val oiditPerViesti = 100

  def log(
    operation: KoskiOperation,
    oppijaOid: String,
    opiskeluoikeusOidit: Seq[String],
    extraFields: AuditLogMessage.ExtraFields = Map.empty,
  )(implicit user: Session): Unit = {
    val base: AuditLogMessage.ExtraFields =
      Map[AuditLogMessageField, String](KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid) ++ extraFields
    if (opiskeluoikeusOidit.isEmpty) {
      AuditLog.log(KoskiAuditLogMessage(operation, user, base))
    } else {
      opiskeluoikeusOidit.grouped(oiditPerViesti).foreach { ryhmä =>
        AuditLog.log(KoskiAuditLogMessage(operation, user, base + (KoskiAuditLogMessageField.opiskeluoikeusOid -> ryhmä.mkString(","))))
      }
    }
  }
}

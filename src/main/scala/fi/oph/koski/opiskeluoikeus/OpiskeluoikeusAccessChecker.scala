package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{Opiskeluoikeus}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSession): Boolean = {
    val orgWriteAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasWriteAccess(o.oid))
    val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTiedonsiirronMitätöintiAccess(o.oid))
    val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
    val sisältääValmiitaSuorituksia = opiskeluoikeus.suoritukset.exists(_.valmis) || opiskeluoikeus.suoritukset.exists(_.osasuoritukset.exists(_.exists(_.valmis)))
    ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess)) && !sisältääValmiitaSuorituksia
  }
}

package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSession): Boolean = {
    val orgWriteAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasWriteAccess(o.oid))
    val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTiedonsiirronMitätöintiAccess(o.oid))
    val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
    val koskeenTallennettava = opiskeluoikeus.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
    val sisältääValmiitaSuorituksia = opiskeluoikeus.suoritukset.exists(_.valmis) || opiskeluoikeus.suoritukset.exists(_.osasuoritukset.exists(_.exists(_.valmis)))
    koskeenTallennettava && ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess)) && !sisältääValmiitaSuorituksia
  }
}

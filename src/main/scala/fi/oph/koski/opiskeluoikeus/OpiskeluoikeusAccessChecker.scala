package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSession): Boolean = {
    val orgWriteAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasWriteAccess(o.oid, opiskeluoikeus.koulutustoimija.map(_.oid)))
    val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTiedonsiirronMitätöintiAccess(o.oid, opiskeluoikeus.koulutustoimija.map(_.oid)))
    val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
    val koskeenTallennettava = opiskeluoikeus.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
    koskeenTallennettava && ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess))
  }
}

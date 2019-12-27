package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSession): Boolean = {
    val orgWriteAccess = opiskeluoikeus.oppilaitosPath.exists(session.hasCreateAndUpdateAccess)
    val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus.oppilaitosPath.exists(session.hasTiedonsiirronMitätöintiAccess)
    val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
    val koskeenTallennettava = opiskeluoikeus.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
    koskeenTallennettava && ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess))
  }
}

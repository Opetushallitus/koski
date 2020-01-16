package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema.{EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, Oppilaitos}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSession): Boolean = {
    val orgWriteAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => hasWriteAccess(session, opiskeluoikeus, o))
    val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTiedonsiirronMitätöintiAccess(o.oid, opiskeluoikeus.koulutustoimija.map(_.oid)))
    val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
    val koskeenTallennettava = opiskeluoikeus.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
    koskeenTallennettava && ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess))
  }

  private def hasWriteAccess(session: KoskiSession, opiskeluoikeus: Opiskeluoikeus, oppilaitos: Oppilaitos) = {
    val koulutustoimijaOid = opiskeluoikeus.koulutustoimija.map(_.oid)
    opiskeluoikeus match {
      case e: EsiopetuksenOpiskeluoikeus if e.järjestämismuoto.isDefined =>
        koulutustoimijaOid.exists(kt => session.hasVarhaiskasvatusAccess(kt, oppilaitos.oid, AccessType.write))
      case _ => session.hasWriteAccess(oppilaitos.oid, koulutustoimijaOid)
    }
  }
}

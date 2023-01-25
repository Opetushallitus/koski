package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.{EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, Oppilaitos, YlioppilastutkinnonOpiskeluoikeus, TaiteenPerusopetuksenOpiskeluoikeus}

object OpiskeluoikeusAccessChecker {
  def isInvalidatable(opiskeluoikeus: Opiskeluoikeus, session: KoskiSpecificSession): Boolean = {
    opiskeluoikeus match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        // TODO: TOR-1639 toistaiseksi YO-tutkinnon opiskeluoikeutta ei voi mitätöidä.
        false
      case _ =>
        val orgWriteAccess = opiskeluoikeus.omistajaOrganisaatio.exists(o => hasWriteAccess(session, opiskeluoikeus, o))
        val orgTiedonsiirronMitätöintiAccess = opiskeluoikeus match {
          case t: TaiteenPerusopetuksenOpiskeluoikeus => opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTaiteenPerusopetusAccess(o.oid, t.koulutustoimija.map(_.oid), AccessType.tiedonsiirronMitätöinti))
          case _ => opiskeluoikeus.omistajaOrganisaatio.exists(o => session.hasTiedonsiirronMitätöintiAccess(o.oid, opiskeluoikeus.koulutustoimija.map(_.oid)))
        }
        val lähdejärjestelmällinen = opiskeluoikeus.lähdejärjestelmänId.nonEmpty
        val koskeenTallennettava = opiskeluoikeus.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
        koskeenTallennettava && ((!lähdejärjestelmällinen && orgWriteAccess) || (lähdejärjestelmällinen && orgTiedonsiirronMitätöintiAccess))
    }
  }

  private def hasWriteAccess(session: KoskiSpecificSession, opiskeluoikeus: Opiskeluoikeus, oppilaitos: Oppilaitos): Boolean = {
    val koulutustoimijaOid = opiskeluoikeus.koulutustoimija.map(_.oid)
    opiskeluoikeus match {
      case e: EsiopetuksenOpiskeluoikeus if e.järjestämismuoto.isDefined =>
        koulutustoimijaOid.exists(kt => session.hasVarhaiskasvatusAccess(kt, oppilaitos.oid, AccessType.write))
      case t: TaiteenPerusopetuksenOpiskeluoikeus if t.onHankintakoulutus && !session.hasKoulutustoimijaOrganisaatioTaiGlobaaliWriteAccess => false
      case _: TaiteenPerusopetuksenOpiskeluoikeus => session.hasTaiteenPerusopetusAccess(oppilaitos.oid, koulutustoimijaOid, AccessType.write)
      case _ => session.hasWriteAccess(oppilaitos.oid, koulutustoimijaOid)
    }
  }
}

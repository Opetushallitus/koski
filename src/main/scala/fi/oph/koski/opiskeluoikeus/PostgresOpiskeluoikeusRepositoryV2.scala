package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.PerustiedotSyncRepository
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, MuunAmmatillisenKoulutuksenSuoritus, MuunKuinSäännellynKoulutuksenOpiskeluoikeus, TaiteenPerusopetuksenOpiskeluoikeus}
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream

class PostgresOpiskeluoikeusRepositoryV2(
  override val db: DB,
  historyRepository: OpiskeluoikeusHistoryRepository,
  henkilöCache: KoskiHenkilöCache,
  oidGenerator: OidGenerator,
  henkilöRepository: OpintopolkuHenkilöRepository,
  perustiedotSyncRepository: PerustiedotSyncRepository,
  organisaatioRepository: OrganisaatioRepository,
  ePerusteetValidator: EPerusteetOpiskeluoikeusChangeValidator
) extends PostgresOpiskeluoikeusRepository(
    db,
    historyRepository,
    henkilöCache,
    oidGenerator,
    henkilöRepository,
    perustiedotSyncRepository,
    organisaatioRepository,
    ePerusteetValidator
  ) {

  override protected def createOrUpdateActionBasedOnDbResult(oppijaOid: PossiblyUnverifiedHenkilöOid,
                                                             opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                                             allowUpdate: Boolean, allowDeleteCompleted:
                               Boolean, rows: Either[HttpStatus, List[OpiskeluoikeusRow]])(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    (allowUpdate, rows) match {
      case (false, Right(r)) if r.nonEmpty && vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus) => createAction(oppijaOid, opiskeluoikeus)
      case _ => super.createOrUpdateActionBasedOnDbResult(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted, rows)
    }
  }

  def vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus) || isMuuKuinSäänneltyKoulutus(opiskeluoikeus) || isTaiteenPerusopetus(opiskeluoikeus)

  def isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus match {
      case _: AmmatillinenOpiskeluoikeus => opiskeluoikeus.suoritukset.forall {
        case _: MuunAmmatillisenKoulutuksenSuoritus => true
        case _ => false
      }
      case _ => false
    }

  def isMuuKuinSäänneltyKoulutus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.isInstanceOf[MuunKuinSäännellynKoulutuksenOpiskeluoikeus]

  def isTaiteenPerusopetus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.isInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]
}

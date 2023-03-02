package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.KoskiOpiskeluoikeusHistoryRepository
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.PerustiedotSyncRepository
import fi.oph.koski.schema._
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream

class PostgresKoskiOpiskeluoikeusRepositoryActionsV2(
  db: DB,
  oidGenerator: OidGenerator,
  henkilöRepository: OpintopolkuHenkilöRepository,
  henkilöCache: KoskiHenkilöCache,
  historyRepository: KoskiOpiskeluoikeusHistoryRepository,
  tableCompanion: OpiskeluoikeusTableCompanion[KoskiOpiskeluoikeusRow],
  organisaatioRepository: OrganisaatioRepository,
  ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  perustiedotSyncRepository: PerustiedotSyncRepository,
  config: Config
) extends PostgresKoskiOpiskeluoikeusRepositoryActions(
  db,
  oidGenerator,
  henkilöRepository,
  henkilöCache,
  historyRepository,
  tableCompanion,
  organisaatioRepository,
  ePerusteetChangeValidator,
  perustiedotSyncRepository,
  config) {

  protected override def createOrUpdateActionBasedOnDbResult(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean,
    rows: Either[HttpStatus, List[KoskiOpiskeluoikeusRow]]
  )(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    (allowUpdate, rows) match {
      case (false, Right(r)) if r.nonEmpty && vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus) => createAction(oppijaOid, opiskeluoikeus)
      case _ => super.createOrUpdateActionBasedOnDbResult(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted, rows)
    }
  }

  private def vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus) || isMuuKuinSäänneltyKoulutus(opiskeluoikeus) || isTaiteenPerusopetus(opiskeluoikeus)

  private def isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus match {
      case _: AmmatillinenOpiskeluoikeus => opiskeluoikeus.suoritukset.forall {
        case _: MuunAmmatillisenKoulutuksenSuoritus => true
        case _ => false
      }
      case _ => false
    }

  private def isMuuKuinSäänneltyKoulutus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.isInstanceOf[MuunKuinSäännellynKoulutuksenOpiskeluoikeus]

  private def isTaiteenPerusopetus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.isInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]
}

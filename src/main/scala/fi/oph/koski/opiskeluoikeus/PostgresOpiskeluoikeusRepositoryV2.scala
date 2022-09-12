package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.DB
import fi.oph.koski.db._
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.eperusteetvalidation.EPerusteetValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.PerustiedotSyncRepository
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, MuunAmmatillisenKoulutuksenSuoritus}
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream

class PostgresOpiskeluoikeusRepositoryV2(override val db: DB,
                                         historyRepository: OpiskeluoikeusHistoryRepository,
                                         henkilöCache: KoskiHenkilöCache,
                                         oidGenerator: OidGenerator,
                                         henkilöRepository: OpintopolkuHenkilöRepository,
                                         perustiedotSyncRepository: PerustiedotSyncRepository,
                                         organisaatioRepository: OrganisaatioRepository,
                                         ePerusteetValidator: EPerusteetValidator)
  extends PostgresOpiskeluoikeusRepository(db,
    historyRepository,
    henkilöCache,
    oidGenerator,
    henkilöRepository,
    perustiedotSyncRepository,
    organisaatioRepository,
    ePerusteetValidator) {

  override protected def createOrUpdateActionBasedOnDbResult(oppijaOid: PossiblyUnverifiedHenkilöOid,
                                                             opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                                             allowUpdate: Boolean, allowDeleteCompleted:
                               Boolean, rows: Either[HttpStatus, List[OpiskeluoikeusRow]])(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    (allowUpdate, rows) match {
      case (false, Right(r)) if r.length > 0 && isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus) => createAction(oppijaOid, opiskeluoikeus)
      case _ => super.createOrUpdateActionBasedOnDbResult(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted, rows)
    }
  }

  def isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = opiskeluoikeus match {
    case _: AmmatillinenOpiskeluoikeus => opiskeluoikeus.suoritukset.forall { _ match {
      case _: MuunAmmatillisenKoulutuksenSuoritus => true
      case _ =>false
    }}
    case _ => false
  }
}

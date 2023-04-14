package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{KoskiOpiskeluoikeusHistoryRepository, OpiskeluoikeusHistory}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedot, PerustiedotSyncRepository}
import fi.oph.koski.schema._
import org.json4s.{JArray, JValue}
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}

class PostgresKoskiOpiskeluoikeusRepositoryActions(
  val db: DB,
  val oidGenerator: OidGenerator,
  val henkilöRepository: OpintopolkuHenkilöRepository,
  val henkilöCache: KoskiHenkilöCache,
  val historyRepository: KoskiOpiskeluoikeusHistoryRepository,
  val tableCompanion: OpiskeluoikeusTableCompanion[KoskiOpiskeluoikeusRow],
  val organisaatioRepository: OrganisaatioRepository,
  val ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  val perustiedotSyncRepository: PerustiedotSyncRepository,
  val config: Config
) extends PostgresOpiskeluoikeusRepositoryActions[KoskiOpiskeluoikeusRow, KoskiOpiskeluoikeusTable, KoskiOpiskeluoikeusHistoryTable] {
  lazy val validator = new OpiskeluoikeusChangeValidator(organisaatioRepository, ePerusteetChangeValidator, config)

  protected def Opiskeluoikeudet = KoskiOpiskeluOikeudet
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession) = KoskiOpiskeluOikeudetWithAccessCheck

  protected def saveHistory(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int = {
    errorRepository.save(opiskeluoikeus, historia, diff)
  }

  protected def syncAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    result: Either[HttpStatus, CreateOrUpdateResult]
  )(implicit user: KoskiSpecificSession): DBIOAction[Any, NoStream, Read with Write] = {
    result match {
      case Right(result) if result.changed =>
        syncHenkilötiedotAction(result.id, oppijaOid.oppijaOid, opiskeluoikeus, result.henkilötiedot)
      case _ =>
        DBIO.successful(Unit)
    }
  }

  private def syncHenkilötiedotAction(id: Int, oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, henkilötiedot: Option[OppijaHenkilöWithMasterInfo]) = {
    henkilötiedot match {
      case _ if opiskeluoikeus.mitätöity && opiskeluoikeus.suoritukset.exists(_.tyyppi.koodiarvo == "vstvapaatavoitteinenkoulutus") =>
        perustiedotSyncRepository.addDeleteToSyncQueue(id)
      case Some(henkilö) =>
        val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, henkilö)
        perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
      case None =>
        henkilöCache.getCachedAction(oppijaOid).flatMap {
          case Some(HenkilöRowWithMasterInfo(henkilöRow, masterHenkilöRow)) =>
            val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, henkilöRow, masterHenkilöRow)
            perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
          case None =>
            throw new RuntimeException(s"Oppija not found: $oppijaOid")
        }
    }
  }

  protected override def createInsteadOfUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    rows: List[KoskiOpiskeluoikeusRow]
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write] = {
    val opiskeluoikeusPäättynyt = rows.exists(_.toOpiskeluoikeusUnsafe.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt)

    if (opiskeluoikeusPäättynyt) {
      createAction(oppijaOid, opiskeluoikeus)
    } else {
      DBIO.successful(Left(KoskiErrorCategory.conflict.exists())) // Ei tehdä uutta, koska vanha vastaava opiskeluoikeus on voimassa
    }
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateKoskiOid(oppija.henkilö.oid)
  }
}

package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{OpiskeluoikeusHistory, YtrOpiskeluoikeusHistoryRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import org.json4s.{JArray, JValue}
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}

class PostgresYtrOpiskeluoikeusRepositoryActions(
  val db: DB,
  val oidGenerator: OidGenerator,
  val henkilöRepository: OpintopolkuHenkilöRepository,
  val henkilöCache: KoskiHenkilöCache,
  val historyRepository: YtrOpiskeluoikeusHistoryRepository,
  val tableCompanion: OpiskeluoikeusTableCompanion[YtrOpiskeluoikeusRow],
  val organisaatioRepository: OrganisaatioRepository,
  val ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  val config: Config
) extends PostgresOpiskeluoikeusRepositoryActions[YtrOpiskeluoikeusRow, YtrOpiskeluoikeusTable, YtrOpiskeluoikeusHistoryTable] {
  lazy val validator = new OpiskeluoikeusChangeValidator(organisaatioRepository, ePerusteetChangeValidator, config)

  protected def Opiskeluoikeudet = YtrOpiskeluOikeudet
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession) = YtrOpiskeluOikeudetWithAccessCheck

  protected def saveHistory(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int = {
    errorRepository.saveYtr(opiskeluoikeus, historia, diff)
  }

  protected def syncAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    result: Either[HttpStatus, CreateOrUpdateResult]
  )(implicit user: KoskiSpecificSession): DBIOAction[Any, NoStream, Read with Write] = {
    DBIO.successful(Unit)
  }

  protected override def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    val identifier = OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)

    findByIdentifierAction(identifier)
      .flatMap {
        case Right(Nil) =>
          createAction(oppijaOid, opiskeluoikeus)
        case Right(aiemmatOpiskeluoikeudet) if allowUpdate =>
          updateIfUnambiguousAiempiOpiskeluoikeusAction(oppijaOid, opiskeluoikeus, aiemmatOpiskeluoikeudet, allowDeleteCompleted)
        case Right(_) =>
          DBIO.successful(Left(KoskiErrorCategory.conflict.exists())) // Ei tehdä uutta, koska vastaava vanha YO-opiskeluoikeus on olemassa
        case Left(err) =>
          DBIO.successful(Left(err))
      }
  }

  private def updateIfUnambiguousAiempiOpiskeluoikeusAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    aiemmatOpiskeluoikeudet: List[YtrOpiskeluoikeusRow],
    allowDeleteCompleted: Boolean
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    aiemmatOpiskeluoikeudet match {
      case List(vanhaOpiskeluoikeus) =>
        updateIfSameOppijaAction(oppijaOid, vanhaOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted)
      case _ =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.löytyiEnemmänKuinYksiRivi(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${aiemmatOpiskeluoikeudet.map(_.oid)})")))
    }
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateYtrOid(oppija.henkilö.oid)
  }
}

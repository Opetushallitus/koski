package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.fixture.ValidationTestContext
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{KoskiOpiskeluoikeusHistoryRepository, OpiskeluoikeusHistory}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedot, PerustiedotSyncRepository}
import fi.oph.koski.schema._
import org.json4s._
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
  val config: Config,
  val validationConfig: ValidationTestContext,
) extends PostgresOpiskeluoikeusRepositoryActions[KoskiOpiskeluoikeusRow, KoskiOpiskeluoikeusTable, KoskiOpiskeluoikeusHistoryTable] {
  lazy val validator = new OpiskeluoikeusChangeValidator(organisaatioRepository, ePerusteetChangeValidator, config)

  protected def Opiskeluoikeudet = KoskiOpiskeluOikeudet
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession) = KoskiOpiskeluOikeudetWithAccessCheck

  protected def saveHistoryError(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int = {
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
      case _ if opiskeluoikeus.mitätöity &&
        opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo).exists(Set("vstvapaatavoitteinenkoulutus", "vstosaamismerkki").contains) =>
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

  protected override def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean,
    skipValidations: Boolean = false
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    val identifier = OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)

    findByIdentifierAction(identifier).flatMap {
      case Right(Nil) =>
        createAction(oppijaOid, opiskeluoikeus)
      case Right(aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet) if allowUpdate =>
        updateIfUnambiguousAiempiOpiskeluoikeusAction(oppijaOid, opiskeluoikeus, identifier, aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet, allowDeleteCompleted, skipValidations)
      case Right(_) =>
        createAction(oppijaOid, opiskeluoikeus)
      case Left(err) =>
        DBIO.successful(Left(err))
    }
  }

  private def updateIfUnambiguousAiempiOpiskeluoikeusAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    identifier: OpiskeluoikeusIdentifier,
    aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet: List[KoskiOpiskeluoikeusRow],
    allowDeleteCompleted: Boolean,
    skipValidations: Boolean = false
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    (identifier, aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet) match {
      case (id: OppijaOidOrganisaatioJaTyyppi, _) =>
        DBIOAction.successful(Left(KoskiErrorCategory.conflict.exists("" +
          s"Olemassaolevan opiskeluoikeuden päivitystä ilman tunnistetta ei tueta. Päivitettävä opiskeluoikeus-oid: ${aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet.map(_.oid).mkString(", ")}. Päivittävä tunniste: ${id.copy(oppijaOid = "****")}"
        )))
      case (_, List(aiempiSamaksiJonkinIdnPerusteellaTunnistettuOpiskeluoikeus)) =>
        updateIfSameOppijaAction(oppijaOid, aiempiSamaksiJonkinIdnPerusteellaTunnistettuOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted, skipValidations)
      case _ =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.löytyiEnemmänKuinYksiRivi(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${aiemmatSamaksiJonkinIdnPerusteellaTunnistetutOpiskeluoikeudet.map(_.oid)})")))
    }
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateKoskiOid(oppija.henkilö.oid)
  }
}

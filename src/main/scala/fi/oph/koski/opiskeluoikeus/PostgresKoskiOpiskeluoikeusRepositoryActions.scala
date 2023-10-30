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
import fi.oph.scalaschema.Serializer.format
import org.json4s._
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}

import java.time.LocalDate

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

  protected override def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    val identifier = OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)

    findByIdentifierAction(identifier).flatMap {
      case Right(Nil) =>
        createAction(oppijaOid, opiskeluoikeus)
      case Right(aiemmatOpiskeluoikeudet) if allowUpdate =>
        updateAction(oppijaOid, opiskeluoikeus, identifier, aiemmatOpiskeluoikeudet, allowDeleteCompleted)
      case Right(aiemmatOpiskeluoikeudet) if vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus, aiemmatOpiskeluoikeudet) =>
        createAction(oppijaOid, opiskeluoikeus)
      case Right(_) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.exists()))
      case Left(err) =>
        DBIO.successful(Left(err))
    }
  }

  private def updateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    identifier: OpiskeluoikeusIdentifier,
    aiemmatOpiskeluoikeudet: List[KoskiOpiskeluoikeusRow],
    allowDeleteCompleted: Boolean
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    (identifier, aiemmatOpiskeluoikeudet) match {
      case (id: OppijaOidOrganisaatioJaTyyppi, _) =>
        DBIOAction.successful(Left(KoskiErrorCategory.conflict.exists("" +
          s"Olemassaolevan opiskeluoikeuden päivitystä ilman tunnistetta ei tueta. Päivitettävä opiskeluoikeus-oid: ${aiemmatOpiskeluoikeudet.map(_.oid).mkString(", ")}. Päivittävä tunniste: ${id.copy(oppijaOid = "****")}"
        )))
      case (_, List(vanhaOpiskeluoikeus)) =>
        updateAction(oppijaOid, vanhaOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted)
      case _ =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.löytyiEnemmänKuinYksiRivi(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${aiemmatOpiskeluoikeudet.map(_.oid)})")))
    }
  }

  private def vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, rows: List[KoskiOpiskeluoikeusRow])(implicit user: KoskiSpecificSession): Boolean = {
    lazy val aiempiOpiskeluoikeusPäättynyt = rows.exists(_.toOpiskeluoikeusUnsafe.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt)

    opiskeluoikeus match {
      case _: MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
        true
      case _: TaiteenPerusopetuksenOpiskeluoikeus =>
        true
      case oo: VapaanSivistystyönOpiskeluoikeus =>
        isJotpa(oo) || aiempiOpiskeluoikeusPäättynyt
      case oo: AmmatillinenOpiskeluoikeus =>
        isMuuAmmatillinenOpiskeluoikeus(oo) || (aiempiOpiskeluoikeusPäättynyt && isAmmatillinenJolleAiemmanOpiskeluoikeudenPäätyttyäRinnakkainenOpiskeluoikeusSallitaan(oo, rows))
      case _ =>
        aiempiOpiskeluoikeusPäättynyt
    }
  }

  private def isMuuAmmatillinenOpiskeluoikeus(opiskeluoikeus: AmmatillinenOpiskeluoikeus): Boolean =
    opiskeluoikeus.suoritukset.forall {
      case _: MuunAmmatillisenKoulutuksenSuoritus => true
      case _ => false
    }

  private def isJotpa(opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus): Boolean =
    opiskeluoikeus.suoritukset.forall {
      case _: VapaanSivistystyönJotpaKoulutuksenSuoritus => true
      case _ => false
    }

  private def isAmmatillinenJolleAiemmanOpiskeluoikeudenPäätyttyäRinnakkainenOpiskeluoikeusSallitaan(opiskeluoikeus: AmmatillinenOpiskeluoikeus, rows: List[KoskiOpiskeluoikeusRow]): Boolean = {
    rows.exists(row => allowOpiskeluoikeusCreationOnConflict(opiskeluoikeus, row))
  }

  private def allowOpiskeluoikeusCreationOnConflict(oo: AmmatillinenOpiskeluoikeus, row: KoskiOpiskeluoikeusRow): Boolean = {
    lazy val perusteenDiaarinumero: Option[String] = {
      val value = (row.data \ "suoritukset")(0) \ "koulutusmoduuli" \ "perusteenDiaarinumero"
      Option(value.extract[String])
    }

    // Jos oppilaitos ja perusteen diaarinumero ovat samat, ei sallita päällekkäisen opiskeluoikeuden luontia...
    (!oo.oppilaitos.exists(_.oid == row.oppilaitosOid) || // Tänne ei pitäisi tulla eriävällä oppilaitoksella, mutta tulevien mahdollisten muutosten varalta tehdään tässä eksplisiittinen tarkastus
      !oo.suoritukset
        .collect { case s: AmmatillisenTutkinnonSuoritus => s }
        .exists(s =>
          s.koulutusmoduuli.perusteenDiaarinumero.isDefined &&
            s.koulutusmoduuli.perusteenDiaarinumero == perusteenDiaarinumero

        )) ||
      // ...paitsi jos ne ovat toisistaan ajallisesti täysin erillään
      !Aikajakso(oo.alkamispäivä.getOrElse(LocalDate.of(0, 1, 1)), oo.päättymispäivä)
        .overlaps(Aikajakso(row.alkamispäivä.toLocalDate, row.päättymispäivä.map(_.toLocalDate)))
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateKoskiOid(oppija.henkilö.oid)
  }
}

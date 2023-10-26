package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{JsonPatchException, OpiskeluoikeusHistory, OpiskeluoikeusHistoryRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonDiff.jsonDiff
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema._
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JObject, JString, JValue}
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}

import java.sql.SQLException

trait PostgresOpiskeluoikeusRepositoryActions[OOROW <: OpiskeluoikeusRow, OOTABLE <: OpiskeluoikeusTable[OOROW], HISTORYTABLE <: OpiskeluoikeusHistoryTable]
  extends DatabaseExecutionContext
    with Logging
    with QueryMethods
{
  def db: DB
  def oidGenerator: OidGenerator
  def henkilöRepository: OpintopolkuHenkilöRepository
  def henkilöCache: KoskiHenkilöCache
  def historyRepository: OpiskeluoikeusHistoryRepository[HISTORYTABLE, OOROW, OOTABLE]
  def tableCompanion: OpiskeluoikeusTableCompanion[OOROW]
  def validator: OpiskeluoikeusChangeValidator
  def config: Config

  protected lazy val errorRepository = new OpiskeluoikeushistoriaErrorRepository(db)

  protected def Opiskeluoikeudet: TableQuery[OOTABLE]
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession): Query[OOTABLE, OOROW, Seq]

  def findByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus] = {
    runDbSync(findByOppijaOidsAction(oids).map(rows => rows.sortBy(_.id).map(_.toOpiskeluoikeusUnsafe)))
  }

  def findByOppijaOidsAction(oids: List[String])(implicit user: KoskiSpecificSession): dbio.DBIOAction[Seq[OOROW], NoStream, Read] = {
    OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid inSetBind oids).result
  }

  protected def findOpiskeluoikeudetWithSlaves(oid: String)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Seq[OOROW], NoStream, Read] =
    (Henkilöt.filter(_.masterOid === oid) ++ Henkilöt.filter(_.oid === oid)).map(_.oid)
      .flatMap(oid => OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid))
      .result

  private def oppijaOidsByOppijaOid(oid: String): DBIOAction[List[String], NoStream, Read] = {
    Henkilöt.filter(_.oid === oid).result.map { henkilöt =>
      henkilöt.headOption match {
        case Some(h) =>
          h.oid :: h.masterOid.toList
        case None =>
          throw new RuntimeException(s"Oppija not found: $oid")
      }
    }
  }

  def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult] = {
    def createOrUpdateWithRetry: Either[HttpStatus, CreateOrUpdateResult] = {
      val result = try {
        runDbSync {
          (for {
            result <- createOrUpdateAction(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted)
            syncAction <- syncAction(oppijaOid, opiskeluoikeus, result)
          } yield result).transactionally
        }
      } catch {
        case e: SQLException if e.getSQLState == "23505" => // 23505 = Unique constraint violation
          if (e.getMessage.contains("""duplicate key value violates unique constraint "opiskeluoikeus_oid_key"""") ||
            e.getMessage.contains("""duplicate key value violates unique constraint "ytr_opiskeluoikeus_oid_key"""")) {
            Left(KoskiErrorCategory.conflict("duplicate oid"))
          } else {
            Left(KoskiErrorCategory.conflict.samanaikainenPäivitys())
          }
        case e: SQLException if e.getMessage.contains("unsupported Unicode escape sequence") =>
          Left(KoskiErrorCategory.badRequest.format.json("unsupported unicode escape sequence in data"))
      }

      if (result.left.exists(_ == KoskiErrorCategory.conflict("duplicate oid"))) {
        createOrUpdateWithRetry
      } else {
        result
      }
    }

    if (!allowUpdate && opiskeluoikeus.oid.isDefined) {
      Left(KoskiErrorCategory.badRequest("Uutta opiskeluoikeutta luotaessa ei hyväksytä arvoja oid-kenttään"))
    } else {
      createOrUpdateWithRetry
    }
  }

  protected def syncAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    result: Either[HttpStatus, CreateOrUpdateResult]
  )(implicit user: KoskiSpecificSession): DBIOAction[Any, NoStream, Read with Write]

  protected def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean = false
  )(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional]

  protected def findByIdentifierAction(identifier: OpiskeluoikeusIdentifier)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, List[OOROW]], NoStream, Read] = {
    identifier match {
      case OpiskeluoikeusByOid(oid) => OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid).result.map { rows =>
        rows.headOption match {
          case Some(oikeus) => Right(List(oikeus))
          case None => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }
      }

      case OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId, oppilaitosOid) =>
        findOpiskeluoikeudetWithSlaves(oppijaOid).map(_.filter { row =>
          row.toOpiskeluoikeusUnsafe.lähdejärjestelmänId.contains(lähdejärjestelmäId) && (oppilaitosOid.isEmpty || oppilaitosOid.contains(row.oppilaitosOid))
        }).map(_.toList).map(Right(_))

      case i:OppijaOidOrganisaatioJaTyyppi =>
        findOpiskeluoikeudetWithSlaves(i.oppijaOid).map(_.filter { row =>
          val opiskeluoikeus = row.toOpiskeluoikeusUnsafe
          OppijaOidOrganisaatioJaTyyppi(i.oppijaOid,
            opiskeluoikeus.getOppilaitos.oid,
            opiskeluoikeus.koulutustoimija.map(_.oid),
            opiskeluoikeus.tyyppi.koodiarvo,
            opiskeluoikeus.suoritukset.headOption.map(_.koulutusmoduuli.tunniste.koodiarvo),
            opiskeluoikeus.suoritukset.headOption.map(_.tyyppi.koodiarvo),
            opiskeluoikeus.lähdejärjestelmänId) == identifier
        }).map(_.toList).map(Right(_))
    }
  }

  protected def createOrUpdateActionBasedOnDbResult(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean,
    rows: Either[HttpStatus, List[OOROW]]
  )(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = (allowUpdate, rows) match {
    case (_, Right(Nil)) => createAction(oppijaOid, opiskeluoikeus)
    case (true, Right(List(vanhaOpiskeluoikeus))) =>
      if (oppijaOid.oppijaOid == vanhaOpiskeluoikeus.oppijaOid) {
        updateAction(vanhaOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted)
      } else { // Check if oppija oid belongs to master of slave oppija oids
        oppijaOidsByOppijaOid(vanhaOpiskeluoikeus.oppijaOid).flatMap { oids =>
          if (oids.contains(oppijaOid.oppijaOid)) {
            updateAction(vanhaOpiskeluoikeus, opiskeluoikeus, allowDeleteCompleted)
          } else {
            DBIO.successful(Left(KoskiErrorCategory.forbidden.oppijaOidinMuutos("Oppijan oid: " + oppijaOid.oppijaOid + " ei löydy opiskeluoikeuden oppijan oideista: " + oids.mkString(", "))))
          }
        }
      }
    case (true, Right(rows)) =>
      DBIO.successful(Left(KoskiErrorCategory.conflict.löytyiEnemmänKuinYksiRivi(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${rows.map(_.oid)})")))
    case (false, Right(rows)) =>
      createInsteadOfUpdate(oppijaOid, opiskeluoikeus, rows)
    case (_, Left(err)) => DBIO.successful(Left(err))
  }

  protected def createInsteadOfUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    rows: List[OOROW]
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write]

  protected def createAction(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write] = {
    oppijaOid.verified match {
      case Some(henkilö) =>
        val withMasterInfo = henkilöRepository.withMasterInfo(henkilö)
        henkilöCache.addHenkilöAction(withMasterInfo).flatMap { _ =>
          createAction(withMasterInfo, opiskeluoikeus)
        }
      case None => DBIO.successful(Left(KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.oppijaOid + " ei löydy.")))
    }
  }

  def createOpiskeluoikeusBypassingUpdateCheckForTests(oppija: OppijaHenkilöWithMasterInfo, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult] = {
    if (Environment.isUnitTestEnvironment(config)) {
      runDbSync(createAction(oppija, opiskeluoikeus))
    } else {
      Left(KoskiErrorCategory.internalError("Kielletty operaatio testien ulkopuolella"))
    }
  }

  private def createAction(oppija: OppijaHenkilöWithMasterInfo, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    opiskeluoikeus.versionumero match {
      case Some(versio) if (versio != VERSIO_1) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero(s"Uudelle opiskeluoikeudelle annettu versionumero $versio")))
      case _ if estäOpiskeluoikeudenLuonti(opiskeluoikeus) =>
        DBIO.successful(Left(KoskiErrorCategory.forbidden("Käyttäjällä ei ole riittäviä oikeuksia luoda opiskeluoikeutta")))
      case _ =>
        val tallennettavaOpiskeluoikeus = opiskeluoikeus
        val oid: String = generateOid(oppija)
        val row: OOROW = tableCompanion.makeInsertableRow(oppija.henkilö.oid, oid, tallennettavaOpiskeluoikeus)
        for {
          opiskeluoikeusId <- Opiskeluoikeudet.returning(Opiskeluoikeudet.map(_.id)) += row
          diff = JArray(List(JObject("op" -> JString("add"), "path" -> JString(""), "value" -> row.data)))
          _ <- historyRepository.createAction(opiskeluoikeusId, VERSIO_1, user.oid, diff)
        } yield {
          Right(Created(opiskeluoikeusId, oid, opiskeluoikeus.lähdejärjestelmänId, oppija, VERSIO_1))
        }
    }
  }

  protected def generateOid(oppija: OppijaHenkilöWithMasterInfo): String

  private def estäOpiskeluoikeudenLuonti(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): Boolean = {
    opiskeluoikeus match {
      // Estä kirjoitusoikeus TPO hankintakoulutuksen opiskeluoikeuteen, jos käyttäjällä löytyy editOnly-access
      case t: TaiteenPerusopetuksenOpiskeluoikeus =>
        user.hasTaiteenPerusopetusAccess(t.getOppilaitos.oid, t.koulutustoimija.map(_.oid), AccessType.editOnly)
      case _ => false
    }
  }

  private def updateAction[A <: PäätasonSuoritus](
    oldRow: OOROW,
    uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowDeleteCompletedSuoritukset: Boolean
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write with Transactional with Read] = {
    val (id, oid, versionumero) = (oldRow.id, oldRow.oid, oldRow.versionumero)
    val nextVersionumero = versionumero + 1

    uusiOpiskeluoikeus.versionumero match {
      case Some(requestedVersionumero) if requestedVersionumero != versionumero =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val vanhaOpiskeluoikeus = oldRow.toOpiskeluoikeusUnsafe

        val tallennettavaOpiskeluoikeus = OpiskeluoikeusChangeMigrator.migrate(vanhaOpiskeluoikeus, uusiOpiskeluoikeus, allowDeleteCompletedSuoritukset)

        val onTaiteenPerusopetusOpiskeluoikeus = tallennettavaOpiskeluoikeus.tyyppi.koodiarvo == "taiteenperusopetus"
        val onVstVapaatavoitteinenSuoritus = tallennettavaOpiskeluoikeus.suoritukset.exists(_.tyyppi.koodiarvo == "vstvapaatavoitteinenkoulutus")
        val onMitätöitävissä = OpiskeluoikeusAccessChecker.isInvalidatable(tallennettavaOpiskeluoikeus, user)

        validator.validateOpiskeluoikeusChange(vanhaOpiskeluoikeus, tallennettavaOpiskeluoikeus) match {
          case HttpStatus.ok if tallennettavaOpiskeluoikeus.mitätöity && onTaiteenPerusopetusOpiskeluoikeus && !onMitätöitävissä =>
            DBIO.successful(Left(KoskiErrorCategory.forbidden("Mitätöinti ei sallittu")))
          case HttpStatus.ok =>
            if (tallennettavaOpiskeluoikeus.mitätöity && (onVstVapaatavoitteinenSuoritus || onTaiteenPerusopetusOpiskeluoikeus)) {
              OpiskeluoikeusPoistoUtils
                .poistaOpiskeluOikeus(id, oid, tallennettavaOpiskeluoikeus, nextVersionumero, oldRow.oppijaOid, true, None)
                .map(_ => Right(Updated(
                  id,
                  oid,
                  uusiOpiskeluoikeus.lähdejärjestelmänId,
                  oldRow.oppijaOid,
                  nextVersionumero,
                  vanhaOpiskeluoikeus
                )))
            } else {
              val updatedValues@(newData, _, _, _, _, _, _, _, _, _, _) = tableCompanion.updatedFieldValues(tallennettavaOpiskeluoikeus, nextVersionumero)
              val diff: JArray = jsonDiff(oldRow.data, newData)
              diff.values.length match {
                case 0 =>
                  DBIO.successful(Right(NotChanged(id, oid, uusiOpiskeluoikeus.lähdejärjestelmänId, oldRow.oppijaOid, versionumero)))
                case _ =>
                  for {
                    rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(_.updateableFields).update(updatedValues)
                    _ <- historyRepository.createAction(id, nextVersionumero, user.oid, diff)
                    hist <- historyRepository.findByOpiskeluoikeusOidAction(oid, nextVersionumero)
                  } yield {
                    rowsUpdated match {
                      case 1 =>
                        verifyHistoria(newData, hist)
                        Right(Updated(id, oid, uusiOpiskeluoikeus.lähdejärjestelmänId, oldRow.oppijaOid, nextVersionumero, vanhaOpiskeluoikeus))
                      case x: Int =>
                        throw new RuntimeException("Unexpected number of updated rows: " + x) // throw exception to cause rollback!
                    }
                  }
              }
            }
          case nonOk => DBIO.successful(Left(nonOk))
        }
    }
  }

  private def verifyHistoria(opiskeluoikeusJson: JValue, hist: Option[OpiskeluoikeusHistory]): Unit =
    hist.flatMap(validateHistoria(opiskeluoikeusJson, _))
      .foreach(logger.warn(_))

  private def validateHistoria(opiskeluoikeusJson: JValue, historia: OpiskeluoikeusHistory): Option[String] = try {
    val opiskeluoikeusDiffHistoria = jsonDiff(opiskeluoikeusJson, historia.asOpiskeluoikeusJson)
    if (opiskeluoikeusDiffHistoria.values.nonEmpty) {
      val id = saveHistory(
        opiskeluoikeus = opiskeluoikeusJson,
        historia = historia,
        diff = opiskeluoikeusDiffHistoria,
      )
      Some(s"Virhe opiskeluoikeushistoriarivin tuottamisessa opiskeluoikeudelle ${historia.oid}/${historia.version}: ${JsonMethods.pretty(opiskeluoikeusDiffHistoria)}")
    } else {
      None
    }
  } catch {
    case e: JsonPatchException => Some(s"Virhe opiskeluoikeushistorian validoinnissa: ${e.getMessage}")
  }

  protected def saveHistory(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int
}

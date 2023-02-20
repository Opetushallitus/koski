package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{JsonPatchException, YtrOpiskeluoikeusHistory, YtrOpiskeluoikeusHistoryRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonDiff.jsonDiff
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema._
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JObject, JString, JValue}
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.GetResult

import java.sql.SQLException
import java.time.LocalDate

// TODO: TOR-1639 Jos mahdollista, yhdistä tästä yhteisiä koodiosia PostgresOpiskeluoikeusRepository:n kanssa.
class PostgresYtrOpiskeluoikeusRepository(
  val db: DB,
  historyRepository: YtrOpiskeluoikeusHistoryRepository,
  henkilöCache: KoskiHenkilöCache,
  oidGenerator: OidGenerator,
  henkilöRepository: OpintopolkuHenkilöRepository
) extends KoskiYtrOpiskeluoikeusRepository with DatabaseExecutionContext with QueryMethods with Logging {
  lazy val errorRepository = new OpiskeluoikeushistoriaErrorRepository(db)

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

  override def findByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[YlioppilastutkinnonOpiskeluoikeus] = {
    runDbSync(findByOppijaOidsAction(oids).map(rows => rows.sortBy(_.id).map(_.toOpiskeluoikeusUnsafe)))
  }

  override def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult] = {
    def createOrUpdateWithRetry: Either[HttpStatus, CreateOrUpdateResult] = {
      val result = try {
        runDbSync {
          (for {
            result <- createOrUpdateAction(oppijaOid, opiskeluoikeus)
            // TODO: TOR-1639 Pitäisikö jotain henkilötietoja synkata YTR-opiskeluoikeuden tallentamisen yhteydessä vai ei?
//            syncAction <- result match {
//              case Right(result) if result.changed =>
//                syncHenkilötiedotAction(result.id, oppijaOid.oppijaOid, opiskeluoikeus, result.henkilötiedot)
//              case _ =>
//                DBIO.successful(Unit)
//            }
          } yield result).transactionally
        }
      } catch {
        case e: SQLException if e.getSQLState == "23505" => // 23505 = Unique constraint violation
          if (e.getMessage.contains("""duplicate key value violates unique constraint "opiskeluoikeus_oid_key"""")) {
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

    createOrUpdateWithRetry
  }

  private def findByOppijaOidsAction(oids: List[String])(implicit user: KoskiSpecificSession): dbio.DBIOAction[Seq[YtrOpiskeluoikeusRow], NoStream, Read] = {
    YtrOpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid inSetBind oids).result
  }

  private def findByIdentifierAction(identifier: OpiskeluoikeusIdentifier)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, List[YtrOpiskeluoikeusRow]], NoStream, Read] = {
    identifier match {
      case i:OppijaOidKoulutustoimijaJaTyyppi =>
        findOpiskeluoikeudetWithSlaves(i.oppijaOid).map(_.filter { row =>
          val opiskeluoikeus = row.toOpiskeluoikeusUnsafe
          OppijaOidKoulutustoimijaJaTyyppi(
            i.oppijaOid,
            opiskeluoikeus.koulutustoimija.map(_.oid).get,
            opiskeluoikeus.tyyppi.koodiarvo,
            opiskeluoikeus.suoritukset.headOption.map(_.koulutusmoduuli.tunniste.koodiarvo),
            opiskeluoikeus.suoritukset.headOption.map(_.tyyppi.koodiarvo),
            opiskeluoikeus.lähdejärjestelmänId) == identifier
        }).map(_.toList).map(Right(_))

      case _ =>
        throw new InternalError("Tuntematon identifier-tyyppi")
    }
  }

  private implicit def getLocalDate: GetResult[LocalDate] = GetResult(r => {
    r.getLocalDate("paiva")
  })

  private def findOpiskeluoikeudetWithSlaves(oid: String)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Seq[YtrOpiskeluoikeusRow], NoStream, Read] =
    (Henkilöt.filter(_.masterOid === oid) ++ Henkilöt.filter(_.oid === oid)).map(_.oid)
      .flatMap(oid => YtrOpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid))
      .result

  private def createOrUpdateAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus
  )(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    findByIdentifierAction(OpiskeluoikeusIdentifier(
      oppijaOid.oppijaOid, opiskeluoikeus
    ))
      .flatMap { rows: Either[HttpStatus, List[YtrOpiskeluoikeusRow]] =>
        createOrUpdateActionBasedOnDbResult(oppijaOid, opiskeluoikeus, rows)
      }
  }

  protected def createOrUpdateActionBasedOnDbResult(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus,
    rows: Either[HttpStatus, List[YtrOpiskeluoikeusRow]]
  )(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    rows match {
      case Right(Nil) =>
        createAction(oppijaOid, opiskeluoikeus)
      case Right(List(vanhaOpiskeluoikeus)) =>
        if (oppijaOid.oppijaOid == vanhaOpiskeluoikeus.oppijaOid) {
          updateAction(vanhaOpiskeluoikeus, opiskeluoikeus)
        } else { // Check if oppija oid belongs to master of slave oppija oids
          oppijaOidsByOppijaOid(vanhaOpiskeluoikeus.oppijaOid).flatMap { oids =>
            if (oids.contains(oppijaOid.oppijaOid)) {
              updateAction(vanhaOpiskeluoikeus, opiskeluoikeus)
            } else {
              DBIO.successful(Left(KoskiErrorCategory.forbidden.oppijaOidinMuutos("Oppijan oid: " + oppijaOid.oppijaOid + " ei löydy opiskeluoikeuden oppijan oideista: " + oids.mkString(", "))))
            }
          }
        }
      case Right(rows) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.löytyiEnemmänKuinYksiRivi(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${rows.map(_.oid)})")))
      case Left(err) => DBIO.successful(Left(err))
    }
  }

  protected def createAction(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write] = {
    oppijaOid.verified match {
      case Some(henkilö) =>
        val withMasterInfo = henkilöRepository.withMasterInfo(henkilö)
        henkilöCache.addHenkilöAction(withMasterInfo).flatMap { _ =>
          createAction(withMasterInfo, opiskeluoikeus)
        }
      case None => DBIO.successful(Left(KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.oppijaOid + " ei löydy.")))
    }
  }

  private def createAction(oppija: OppijaHenkilöWithMasterInfo, opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus)(implicit user: KoskiSpecificSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    opiskeluoikeus.versionumero match {
      case Some(versio) if (versio != VERSIO_1) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero(s"Uudelle opiskeluoikeudelle annettu versionumero $versio")))
      case _ =>
        val tallennettavaOpiskeluoikeus = opiskeluoikeus
        val oid = oidGenerator.generateOid(oppija.henkilö.oid)
        val row: YtrOpiskeluoikeusRow = KoskiTables.YtrOpiskeluoikeusTable.makeInsertableRow(oppija.henkilö.oid, oid, tallennettavaOpiskeluoikeus)
        for {
          opiskeluoikeusId <- KoskiTables.YtrOpiskeluOikeudet.returning(YtrOpiskeluOikeudet.map(_.id)) += row
          diff = JArray(List(JObject("op" -> JString("add"), "path" -> JString(""), "value" -> row.data)))
          _ <- historyRepository.createAction(opiskeluoikeusId, VERSIO_1, user.oid, diff)
        } yield {
          Right(Created(opiskeluoikeusId, oid, opiskeluoikeus.lähdejärjestelmänId, oppija, VERSIO_1))
        }
    }
  }

  private def updateAction[A <: PäätasonSuoritus](oldRow: YtrOpiskeluoikeusRow, uusiOpiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus)(implicit user: KoskiSpecificSession) = {
    val (id, oid, versionumero) = (oldRow.id, oldRow.oid, oldRow.versionumero)
    val nextVersionumero = versionumero + 1

    uusiOpiskeluoikeus.versionumero match {
      case Some(requestedVersionumero) if requestedVersionumero != versionumero =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val vanhaOpiskeluoikeus = oldRow.toOpiskeluoikeusUnsafe

        val tallennettavaOpiskeluoikeus = uusiOpiskeluoikeus
        // TODO: TOR-1639 Oletettavasti mitään migraatiota ei tarvitse YTR-opiskeluoikeuksille tehdä
        // OpiskeluoikeusChangeMigrator.migrate(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)

        val updatedValues@(newData, _, _, _, _, _, _, _, _, _, _) = KoskiTables.YtrOpiskeluoikeusTable.updatedFieldValues(tallennettavaOpiskeluoikeus, nextVersionumero)
        val diff: JArray = jsonDiff(oldRow.data, newData)
        diff.values.length match {
          case 0 =>
            DBIO.successful(Right(NotChanged(id, oid, uusiOpiskeluoikeus.lähdejärjestelmänId, oldRow.oppijaOid, versionumero)))
          case _ =>
            for {
              rowsUpdated <- YtrOpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(_.updateableFields).update(updatedValues)
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
  }

  private def verifyHistoria(opiskeluoikeusJson: JValue, hist: Option[YtrOpiskeluoikeusHistory]): Unit =
    hist.flatMap(validateHistoria(opiskeluoikeusJson, _))
        .foreach(logger.warn(_))

  private def validateHistoria(opiskeluoikeusJson: JValue, historia: YtrOpiskeluoikeusHistory): Option[String] = try {
    val opiskeluoikeusDiffHistoria = jsonDiff(opiskeluoikeusJson, historia.asOpiskeluoikeusJson)
    if (opiskeluoikeusDiffHistoria.values.nonEmpty) {
      val id = errorRepository.saveYtr(
        opiskeluoikeus = opiskeluoikeusJson,
        historia = historia,
        diff = opiskeluoikeusDiffHistoria,
      )
      Some(s"Virhe opiskeluoikeushistoriarivin tuottamisessa YTR opiskeluoikeudelle ${historia.oid}/${historia.version}: ${JsonMethods.pretty(opiskeluoikeusDiffHistoria)}")
    } else {
      None
    }
  } catch {
    case e: JsonPatchException => Some(s"Virhe YTR opiskeluoikeushistorian validoinnissa: ${e.getMessage}")
  }
}

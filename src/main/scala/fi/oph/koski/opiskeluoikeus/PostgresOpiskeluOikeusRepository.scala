package fi.oph.koski.opiskeluoikeus

import java.sql.SQLException

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{KoskiHenkilöCacheUpdater, PossiblyUnverifiedHenkilöOid}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusChangeValidator.validateOpiskeluoikeusChange
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema._
import fi.oph.koski.util.OidGenerator
import org.json4s.JArray
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream
import slick.lifted.Query
import slick.{dbio, lifted}

class PostgresOpiskeluoikeusRepository(val db: DB, historyRepository: OpiskeluoikeusHistoryRepository, henkilöCache: KoskiHenkilöCacheUpdater, oidGenerator: OidGenerator) extends OpiskeluoikeusRepository with GlobalExecutionContext with KoskiDatabaseMethods with Logging with SerializableTransactions {
  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession) = {
    val query: lifted.Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq] = for {
      oo <- OpiskeluOikeudetWithAccessCheck
      if oo.oppijaOid inSetBind oppijat.map(_.oid)
    } yield {
      oo
    }
    val oppijatJoillaOpiskeluoikeuksia: Set[String] = runDbSync(query.map(_.oppijaOid).result).toSet
    oppijat.filter { oppija => oppijatJoillaOpiskeluoikeuksia.contains(oppija.oid)}
  }


  override def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = {
    runDbSync(findByOppijaOidAction(oid).map(rows => rows.map(_.toOpiskeluoikeus)))
  }

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = {
    assert(oid == user.oid, "Käyttäjän oid: " + user.oid + " poikkeaa etsittävän oppijan oidista: " + oid)
    runDbSync(findAction(OpiskeluOikeudet.filter(_.oppijaOid === oid)).map(rows => rows.map(_.toOpiskeluoikeus)))
  }

  override def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusRow] =
    if (oid.matches("""^1\.2\.246\.562\.15\.\d{11}$""")) {
      runDbSync(findAction(OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)))
        .headOption
        .toRight(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.15.00000000001."))
    }

  def delete(id: Int)(implicit user: KoskiSession): HttpStatus = {
    runDbSync(OpiskeluOikeudetWithAccessCheck.filter(_.id === id).delete) match {
      case 0 => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
      case 1 => HttpStatus.ok
      case _ => KoskiErrorCategory.internalError()
    }
  }

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult] = {
    def createOrUpdateWithRetry: Either[HttpStatus, CreateOrUpdateResult] = {
      val result = try {
        runDbSync(createOrUpdateAction(oppijaOid, opiskeluoikeus, allowUpdate).transactionally)
      } catch {
        case e: SQLException if e.getSQLState == "23505" => // 23505 = Unique constraint violation
          if (e.getMessage.contains("""duplicate key value violates unique constraint "opiskeluoikeus_oid_key"""")) {
            Left(KoskiErrorCategory.conflict("duplicate oid"))
          } else {
            Left(KoskiErrorCategory.conflict.samanaikainenPäivitys())
          }
      }

      if (result.left.exists(_ == KoskiErrorCategory.conflict("duplicate oid"))) {
        createOrUpdateWithRetry
      } else {
        result
      }
    }

    if (!allowUpdate && (opiskeluoikeus.oid.isDefined || opiskeluoikeus.id.isDefined)) {
      Left(KoskiErrorCategory.badRequest("Uutta opiskeluoikeutta luotaessa ei hyväksytä arvoja oid ja id-kenttiin"))
    } else {
      createOrUpdateWithRetry
    }
  }

  private def findByOppijaOidAction(oid: String)(implicit user: KoskiSession): dbio.DBIOAction[Seq[OpiskeluoikeusRow], NoStream, Read] = {
    findAction(OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid))
  }

  private def findByIdentifierAction(identifier: OpiskeluoikeusIdentifier)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, List[OpiskeluoikeusRow]], NoStream, Read] = {
    identifier match {
      case OpiskeluoikeusOid(oid) => findAction(OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)).map { rows =>
        rows.headOption match {
          case Some(oikeus) => Right(List(oikeus))
          case None => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }
      }

      case OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId) =>
        findByOppijaOidAction(oppijaOid).map(_.filter { row =>
          row.toOpiskeluoikeus.lähdejärjestelmänId == Some(lähdejärjestelmäId)
        }).map(_.toList).map(Right(_))

      case i:OppijaOidOrganisaatioJaTyyppi =>
        findByOppijaOidAction(i.oppijaOid).map(_.filter { row =>
          OppijaOidOrganisaatioJaTyyppi(i.oppijaOid, row.toOpiskeluoikeus.getOppilaitos.oid, row.toOpiskeluoikeus.tyyppi.koodiarvo, row.toOpiskeluoikeus.lähdejärjestelmänId) == identifier
        }).map(_.toList).map(Right(_))
    }
  }

  private def findAction(query: Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq])(implicit user: KoskiSession): dbio.DBIOAction[Seq[OpiskeluoikeusRow], NoStream, Read] = {
    query.result
  }

  private def createOrUpdateAction(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    findByIdentifierAction(OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)).flatMap { rows: Either[HttpStatus, List[OpiskeluoikeusRow]] =>
      (allowUpdate, rows) match {
        case (_, Right(Nil)) => createAction(oppijaOid, opiskeluoikeus)
        case (true, Right(List(vanhaOpiskeluoikeus))) =>
          updateAction(vanhaOpiskeluoikeus, opiskeluoikeus)
        case (true, Right(rows)) =>
          DBIO.successful(Left(KoskiErrorCategory.internalError(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${rows.map(_.id)})")))
        case (false, Right(rows)) =>
          rows.find(!_.toOpiskeluoikeus.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt) match {
            case None => createAction(oppijaOid, opiskeluoikeus) // Tehdään uusi opiskeluoikeus, koska vanha on päättynyt
            case Some(_) => DBIO.successful(Left(KoskiErrorCategory.conflict.exists())) // Ei tehdä uutta, koska vanha vastaava opiskeluoikeus on voimassa
          }
        case (_, Left(err)) => DBIO.successful(Left(err))
      }
    }
  }

  private def createAction(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write] = {
    oppijaOid.verified match {
      case Some(henkilö) =>
        henkilöCache.addHenkilöAction(henkilö).flatMap { _ =>
          createAction(henkilö.oid, opiskeluoikeus)
        }
      case None => DBIO.successful(Left(KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.oppijaOid + " ei löydy.")))
    }
  }

  private def createAction(oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    opiskeluoikeus.versionumero match {
      case Some(versio) if (versio != VERSIO_1) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero(s"Uudelle opiskeluoikeudelle annettu versionumero $versio")))
      case _ =>
        val tallennettavaOpiskeluoikeus = opiskeluoikeus.withIdAndVersion(oid = None, id = None, versionumero = None)
        val oid = oidGenerator.generateOid(oppijaOid)
        val row: OpiskeluoikeusRow = Tables.OpiskeluoikeusTable.makeInsertableRow(oppijaOid, oid, tallennettavaOpiskeluoikeus)
        for {
          opiskeluoikeusId <- Tables.OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += row
          diff = Json.toJValue(List(Map("op" -> "add", "path" -> "", "value" -> row.data)))
          _ <- historyRepository.createAction(opiskeluoikeusId, VERSIO_1, user.oid, diff)
        } yield {
          Right(Created(opiskeluoikeusId, oid, VERSIO_1, diff, row.data))
        }
    }
  }

  private def updateAction[A <: PäätasonSuoritus](oldRow: OpiskeluoikeusRow, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val (id, oid, versionumero) = (oldRow.id, oldRow.oid, oldRow.versionumero)
    val nextVersionumero = versionumero + 1

    uusiOpiskeluoikeus.versionumero match {
      case Some(requestedVersionumero) if (requestedVersionumero != versionumero) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val vanhaOpiskeluoikeus = oldRow.toOpiskeluoikeus

        val täydennettyOpiskeluoikeus = OpiskeluoikeusChangeMigrator.kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus).withVersion(nextVersionumero)

        val updatedValues@(newData, _, _, _, _, _) = Tables.OpiskeluoikeusTable.updatedFieldValues(täydennettyOpiskeluoikeus)

        val diff: JArray = Json.jsonDiff(oldRow.data, newData)
        diff.values.length match {
          case 0 =>
            DBIO.successful(Right(NotChanged(id, oid, versionumero, diff, newData)))
          case _ =>
            validateOpiskeluoikeusChange(vanhaOpiskeluoikeus, täydennettyOpiskeluoikeus) match {
              case HttpStatus.ok =>
                for {
                  rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(_.updateableFields).update(updatedValues)
                  _ <- historyRepository.createAction(id, nextVersionumero, user.oid, diff)
                } yield {
                  rowsUpdated match {
                    case 1 => Right(Updated(id, oid, nextVersionumero, diff, newData, vanhaOpiskeluoikeus))
                    case x: Int =>
                      throw new RuntimeException("Unexpected number of updated rows: " + x) // throw exception to cause rollback!
                  }
                }
              case nonOk => DBIO.successful(Left(nonOk))
            }
        }
    }
  }
}
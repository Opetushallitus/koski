package fi.oph.tor.opiskeluoikeus

import java.sql.SQLException

import com.github.fge.jsonpatch.diff.JsonDiff
import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.history.OpiskeluoikeusHistoryRepository
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus}
import fi.oph.tor.tor.{OpiskeluoikeusPäättynytAikaisintaan, OpiskeluoikeusPäättynytViimeistään, QueryFilter, TutkinnonTila}
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.ReactiveStreamsToRx
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.{JArray, JValue}
import org.json4s.jackson.JsonMethods
import rx.lang.scala.Observable
import slick.dbio
import slick.dbio.Effect.{Transactional, Read, Write}
import slick.jdbc.TransactionIsolation

class PostgresOpiskeluOikeusRepository(db: DB, historyRepository: OpiskeluoikeusHistoryRepository) extends OpiskeluOikeusRepository with Futures with GlobalExecutionContext with Logging {
  // Note: this is a naive implementation. All filtering should be moved to query-level instead of in-memory-level
  override def filterOppijat(oppijat: Seq[FullHenkilö])(implicit user: TorUser) = {
    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = for {
      oo <- OpiskeluOikeudetWithAccessCheck
      if oo.oppijaOid inSetBind oppijat.map(_.oid)
    } yield {
      oo
    }

    //println(query.result.statements.head)

    val oikeudet: Set[String] = await(db.run(query.map(_.oppijaOid).result)).toSet

    oppijat.filter { oppija => oikeudet.contains(oppija.oid)}
  }


  override def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[OpiskeluOikeus] = {
    await(db.run(findByOppijaOidAction(oid).map(rows => rows.map(_.toOpiskeluOikeus))))
  }

  override def find(identifier: OpiskeluOikeusIdentifier)(implicit user: TorUser): Either[HttpStatus, Option[OpiskeluOikeus]] = {
    await(db.run(findByIdentifierAction(identifier).map(result => result.right.map(_.map(_.toOpiskeluOikeus)))))
  }

  override def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[OpiskeluOikeus])] = {
    import ReactiveStreamsToRx._

    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = filters.foldLeft(OpiskeluOikeudetWithAccessCheck.asInstanceOf[Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq]]) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, TutkinnonTila(tila)) => query.filter(_.data.#>>(List("suoritus", "tila", "koodiarvo")) === tila)
    }.sortBy(_.oppijaOid)

    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    val rows: Observable[OpiskeluOikeusRow] = db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount

    val groupedByPerson: Observable[Seq[OpiskeluOikeusRow]] = rows
      .tumblingBuffer(rows.map(_.oppijaOid).distinctUntilChanged.drop(1))

    groupedByPerson.flatMap {
      case oikeudet@(firstRow :: _) =>
        val oppijaOid = firstRow.oppijaOid
        assert(oikeudet.map(_.oppijaOid).toSet == Set(oppijaOid), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijaOid + ": " + oikeudet)
        Observable.just((oppijaOid, oikeudet.map(_.toOpiskeluOikeus).toList))
      case _ => Observable.empty
    }
  }

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = {
    if (!user.userOrganisations.hasReadAccess(opiskeluOikeus.oppilaitos)) {
      Left(HttpStatus.forbidden("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid))
    } else {
      try {
        await(db.run(createOrUpdateAction(oppijaOid, opiskeluOikeus)))
      } catch {
        case e:SQLException if e.getSQLState == "40001" => Left(HttpStatus.conflict("Oppijan " + oppijaOid + " opiskeluoikeuden muutos epäonnistui samanaikaisten muutoksien vuoksi."))
      }
    }
  }

  private def findByOppijaOidAction(oid: String)(implicit user: TorUser): dbio.DBIOAction[Seq[OpiskeluOikeusRow], NoStream, Read] = {
    findAction(OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid))
  }

  private def findByIdentifierAction(identifier: OpiskeluOikeusIdentifier)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, Option[OpiskeluOikeusRow]], NoStream, Read] = identifier match{
    case PrimaryKey(id) => {
      findAction(OpiskeluOikeudetWithAccessCheck.filter(_.id === id)).map { rows =>
        rows.headOption match {
          case Some(oikeus) => Right(Some(oikeus))
          case None => Left(HttpStatus.notFound("Opiskeluoikeus not found for id: " + id))
        }
      }
    }

    case IdentifyingSetOfFields(oppijaOid, _, _, _) => {
      findByOppijaOidAction(oppijaOid)
        .map(rows => Right(rows.find({ row =>
        new IdentifyingSetOfFields(oppijaOid, row.toOpiskeluOikeus) == identifier
      })))
    }
  }

  private def findAction(query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq])(implicit user: TorUser): dbio.DBIOAction[Seq[OpiskeluOikeusRow], NoStream, Read] = {
    query.result
  }

  private def createOrUpdateAction(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    findByIdentifierAction(OpiskeluOikeusIdentifier(oppijaOid.oppijaOid, opiskeluOikeus)).flatMap { rows: Either[HttpStatus, Option[OpiskeluOikeusRow]] =>
      rows match {
        case Right(Some(vanhaOpiskeluOikeus)) =>
          updateAction(vanhaOpiskeluOikeus, opiskeluOikeus)
        case Right(None) =>
          oppijaOid.verifiedOid match {
            case Some(oid) => createAction(oid, opiskeluOikeus)
            case None => DBIO.successful(Left(HttpStatus.notFound("Oppija " + oppijaOid.oppijaOid + " not found")))
          }
        case Left(err) => DBIO.successful(Left(err))
      }
    }.transactionally.withTransactionIsolation(TransactionIsolation.Serializable)
    /*
    1. transactionally = if any part fails, rollback everything. For example, if new version cannot be written to history table, insert/update must be rolled back.
    2. withTransactionIsolation(Serializable) = another concurrent transaction must not be allowed for the same row. otherwise, version history would be messed up.

    This mechanism is tested with Gatling simulation "UpdateSimulation" which causes concurrent updates to the same row.
    */
  }

  private def createAction(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val versionumero = OpiskeluOikeus.VERSIO_1
    val tallennettavaOpiskeluOikeus = opiskeluOikeus.copy(id = None, versionumero = None)
    for {
      opiskeluoikeusId <- OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += new OpiskeluOikeusRow(oppijaOid, tallennettavaOpiskeluOikeus, versionumero)
      diff = Json.toJValue(List(Map("op" -> "add", "path" -> "", "value" -> tallennettavaOpiskeluOikeus)))
      _ <- historyRepository.createAction(opiskeluoikeusId, versionumero, user.oid, diff)
    } yield {
      Right(Created(opiskeluoikeusId, versionumero, diff))
    }
  }

  private def updateAction(oldRow: OpiskeluOikeusRow, uusiOlio: OpiskeluOikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val (id, versionumero) = (oldRow.id, oldRow.versionumero)

    uusiOlio.versionumero match {
      case Some(requestedVersionumero) if (requestedVersionumero != versionumero) => DBIO.successful(Left(HttpStatus.conflict("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val uusiData = Json.toJValue(uusiOlio.copy(id = None, versionumero = None))
        val diff = JsonMethods.fromJsonNode(JsonDiff.asJson(JsonMethods.asJsonNode(oldRow.data), JsonMethods.asJsonNode(uusiData))).asInstanceOf[JArray]
        diff.values.length match {
          case 0 =>
            DBIO.successful(Right(NotChanged(id, versionumero, diff)))
          case more =>
            val nextVersionumero = versionumero + 1
            for {
              rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(row => (row.data, row.versionumero)).update((uusiData, nextVersionumero))
              _ <- historyRepository.createAction(id, nextVersionumero, user.oid, diff)
            } yield {
              rowsUpdated match {
                case 1 => Right(Updated(id, nextVersionumero, diff))
                case x =>
                  throw new RuntimeException("Unexpected number of updated rows: " + x) // throw exception to cause rollback!
              }
            }
        }
    }
  }
}
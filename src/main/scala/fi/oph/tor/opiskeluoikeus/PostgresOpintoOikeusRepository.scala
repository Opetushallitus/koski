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
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import rx.lang.scala.Observable
import slick.dbio
import slick.dbio.Effect.{Read, Write}
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

    val observable: Observable[(String, OpiskeluOikeus)] = db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000)).mapResult { row =>
      (row.oppijaOid, row.toOpiskeluOikeus) // TODO: ehkä siirrä tämäkin käsittely Rx-puolelle
    }.publish.refCount

    val buffered: Observable[List[(String, OpiskeluOikeus)]] = observable.tumblingBuffer(observable.map(_._1).distinctUntilChanged.drop(1)).map(_.toList)

    buffered.flatMap {
      case oikeudet@((personOid, opiskeluOikeus) :: _) =>
        assert(oikeudet.map(_._1).toSet == Set(personOid), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + personOid + ": " + oikeudet)
        Observable.just((personOid, oikeudet.map(_._2)))
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

  private def createOrUpdateAction(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser) = {
    findByIdentifierAction(OpiskeluOikeusIdentifier(oppijaOid.oppijaOid, opiskeluOikeus)).flatMap { rows: Either[HttpStatus, Option[OpiskeluOikeusRow]] =>
      rows match {
        case Right(Some(vanhaOpiskeluOikeus)) =>
          updateAction(vanhaOpiskeluOikeus.id, vanhaOpiskeluOikeus.versionumero + 1, vanhaOpiskeluOikeus.data, Json.toJValue(opiskeluOikeus.copy(id = Some(vanhaOpiskeluOikeus.id)))).map {
            case error if error.isError => Left(error)
            case _ => Right(Updated(vanhaOpiskeluOikeus.id))
          }
        case Right(None) =>
          oppijaOid.verifiedOid match {
            case Some(oid) => createAction(oid, opiskeluOikeus).map(result => result.right.map(Created(_)))
            case None => DBIO.successful(Left(HttpStatus.notFound("Oppija " + oppijaOid.oppijaOid + " not found")))
          }
        case Left(err) => DBIO.successful(Left(err))
      }
    }.transactionally.withTransactionIsolation(TransactionIsolation.Serializable)
  }

  private def createAction(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, Int], NoStream, Write] = {
    val versionumero = 1
    for {
      opiskeluoikeusId <- OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += new OpiskeluOikeusRow(oppijaOid, opiskeluOikeus, versionumero)
      diff = Json.toJValue(List(Map("op" -> "add", "path" -> "", "value" -> opiskeluOikeus.copy(id = Some(opiskeluoikeusId)))))
      _ <- historyRepository.createAction(opiskeluoikeusId, versionumero, user.oid, diff)
    } yield {
      Right(opiskeluoikeusId)
    }
  }

  private def updateAction(id: Int, versionumero: Int, vanhaData: JValue, uusiData: JValue)(implicit user: TorUser): dbio.DBIOAction[HttpStatus, NoStream, Write] = {
    // TODO: always overriding existing data can not be the eventual update strategy
    for {
      rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(row => (row.data, row.versionumero)).update((uusiData, versionumero))
      diff = JsonMethods.fromJsonNode(JsonDiff.asJson(JsonMethods.asJsonNode(vanhaData), JsonMethods.asJsonNode(uusiData)))
      _ <- historyRepository.createAction(id, versionumero, user.oid, diff)
    } yield {
      rowsUpdated match {
        case 1 => HttpStatus.ok
        case x =>
          logger.error("Unexpected number of updated rows: " + x)
          HttpStatus.internalError()
      }
    }
  }
}
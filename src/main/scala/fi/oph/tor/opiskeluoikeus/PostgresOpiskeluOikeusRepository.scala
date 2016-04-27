package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.history.OpiskeluoikeusHistoryRepository
import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema.{KorkeakoulunOpiskeluoikeus, Opiskeluoikeus, TaydellisetHenkilötiedot}
import fi.oph.tor.tor.{OpiskeluoikeusPäättynytAikaisintaan, OpiskeluoikeusPäättynytViimeistään, QueryFilter, TutkinnonTila}
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.ReactiveStreamsToRx
import org.json4s.JArray
import rx.lang.scala.Observable
import slick.dbio
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream

class PostgresOpiskeluOikeusRepository(db: DB, historyRepository: OpiskeluoikeusHistoryRepository) extends OpiskeluOikeusRepository with GlobalExecutionContext with Futures with Logging with SerializableTransactions {
  override def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser) = {
    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = for {
      oo <- OpiskeluOikeudetWithAccessCheck
      if oo.oppijaOid inSetBind oppijat.map(_.oid)
    } yield {
      oo
    }

    //logger.info(query.result.statements.head)

    val oppijatJoillaOpiskeluoikeuksia: Set[String] = await(db.run(query.map(_.oppijaOid).result)).toSet

    oppijat.filter { oppija => oppijatJoillaOpiskeluoikeuksia.contains(oppija.oid)}
  }


  override def findByOppijaOid(oid: String)(implicit user: TorUser): Seq[Opiskeluoikeus] = {
    await(db.run(findByOppijaOidAction(oid).map(rows => rows.map(_.toOpiskeluOikeus))))
  }

  def findById(id: Int)(implicit user: TorUser): Option[(Opiskeluoikeus, String)] = {
    await(db.run(findAction(OpiskeluOikeudetWithAccessCheck.filter(_.id === id)).map(rows => rows.map(row => (row.toOpiskeluOikeus, row.oppijaOid))))).headOption
  }

  override def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[(Oid, List[Opiskeluoikeus])] = {
    import ReactiveStreamsToRx._

    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = filters.foldLeft(OpiskeluOikeudetWithAccessCheck.asInstanceOf[Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq]]) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, TutkinnonTila(tila)) => query.filter(_.data.#>>(List("suoritus", "tila", "koodiarvo")) === tila)
    }.sortBy(_.oppijaOid)

    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    val rows: Observable[OpiskeluOikeusRow] = db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount

    val groupedByPerson: Observable[List[OpiskeluOikeusRow]] = rows
      .tumblingBuffer(rows.map(_.oppijaOid).distinctUntilChanged.drop(1))
      .map(_.toList)

    groupedByPerson.flatMap {
      case oikeudet@(firstRow :: _) =>
        val oppijaOid = firstRow.oppijaOid
        assert(oikeudet.map(_.oppijaOid).toSet == Set(oppijaOid), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijaOid + ": " + oikeudet)
        Observable.just((oppijaOid, oikeudet.map(_.toOpiskeluOikeus).toList))
      case _ =>
        Observable.empty
    }
  }


  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): Either[HttpStatus, CreateOrUpdateResult] = opiskeluOikeus match {
    case o: KorkeakoulunOpiskeluoikeus =>
      Left(TorErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ei voi päivittää Koski-järjestelmässä"))
    case _ =>
      if (!user.hasReadAccess(opiskeluOikeus.oppilaitos)) {
        Left(TorErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid))
      } else {
        doInIsolatedTransaction(db, createOrUpdateAction(oppijaOid, opiskeluOikeus), "Oppijan " + oppijaOid + " opiskeluoikeuden lisäys/muutos")
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
          case None => Left(TorErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + id + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
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

  private def createOrUpdateAction(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    findByIdentifierAction(OpiskeluOikeusIdentifier(oppijaOid.oppijaOid, opiskeluOikeus)).flatMap { rows: Either[HttpStatus, Option[OpiskeluOikeusRow]] =>
      rows match {
        case Right(Some(vanhaOpiskeluOikeus)) =>
          updateAction(vanhaOpiskeluOikeus, opiskeluOikeus)
        case Right(None) =>
          oppijaOid.verifiedOid match {
            case Some(oid) => createAction(oid, opiskeluOikeus)
            case None => DBIO.successful(Left(TorErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.oppijaOid + " ei löydy.")))
          }
        case Left(err) => DBIO.successful(Left(err))
      }
    }
  }

  private def createAction(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val versionumero = Opiskeluoikeus.VERSIO_1
    val tallennettavaOpiskeluOikeus = opiskeluOikeus.withIdAndVersion(id = None, versionumero = None)
    for {
      opiskeluoikeusId <- OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += new OpiskeluOikeusRow(oppijaOid, tallennettavaOpiskeluOikeus, versionumero)
      diff = Json.toJValue(List(Map("op" -> "add", "path" -> "", "value" -> tallennettavaOpiskeluOikeus)))
      _ <- historyRepository.createAction(opiskeluoikeusId, versionumero, user.oid, diff)
    } yield {
      Right(Created(opiskeluoikeusId, versionumero, diff))
    }
  }

  private def updateAction(oldRow: OpiskeluOikeusRow, uusiOlio: Opiskeluoikeus)(implicit user: TorUser): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val (id, versionumero) = (oldRow.id, oldRow.versionumero)

    uusiOlio.versionumero match {
      case Some(requestedVersionumero) if (requestedVersionumero != versionumero) => DBIO.successful(Left(TorErrorCategory.conflict.versionumero("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val uusiData = Json.toJValue(uusiOlio.withIdAndVersion(id = None, versionumero = None))
        val diff: JArray = Json.jsonDiff(oldRow.data, uusiData)
        diff.values.length match {
          case 0 =>
            DBIO.successful(Right(NotChanged(id, versionumero, diff)))
          case _ =>
            val nextVersionumero = versionumero + 1
            for {
              rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(row => (row.data, row.versionumero)).update((uusiData, nextVersionumero))
              _ <- historyRepository.createAction(id, nextVersionumero, user.oid, diff)
            } yield {
              rowsUpdated match {
                case 1 => Right(Updated(id, nextVersionumero, diff))
                case x: Int =>
                  throw new RuntimeException("Unexpected number of updated rows: " + x) // throw exception to cause rollback!
              }
            }
        }
    }
  }
}
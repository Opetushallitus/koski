package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö._
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus}
import fi.oph.tor.tor.{OpiskeluoikeusPäättynytAikaisintaan, OpiskeluoikeusPäättynytViimeistään, QueryFilter, TutkinnonTila}
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.ReactiveStreamsToRx
import fi.vm.sade.utils.slf4j.Logging
import rx.lang.scala.Observable

class PostgresOpiskeluOikeusRepository(db: DB) extends OpiskeluOikeusRepository with Futures with GlobalExecutionContext with Logging {
  // Note: this is a naive implementation. All filtering should be moved to query-level instead of in-memory-level
  override def filterOppijat(oppijat: Seq[FullHenkilö])(implicit userContext: TorUser) = {
    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = for {
      oo <- OpiskeluOikeudet
      if oo.oppijaOid inSetBind oppijat.map(_.oid)
    } yield {
      oo
    }

    //println(query.result.statements.head)

    val fullQuery: Query[Rep[String], String, Seq] = queryWithAccessCheck(query).map(_.oppijaOid)

    val oikeudet: Set[String] = runQuery(fullQuery).toSet

    oppijat.filter { oppija => oikeudet.contains(oppija.oid)}
  }


  override def findByOppijaOid(oid: String)(implicit userContext: TorUser): Seq[OpiskeluOikeus] = {
    find(OpiskeluOikeudet.filter(_.oppijaOid === oid))
  }

  override def create(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): Either[HttpStatus, OpiskeluOikeus.Id] = {
    Right(await(db.run(OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += new OpiskeluOikeusRow(oppijaOid, opiskeluOikeus))))
  }

  // TODO: createOrUpdate should be transactional
  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus)(implicit userContext: TorUser): Either[HttpStatus, CreateOrUpdateResult] = {
    val opiskeluoikeudet: Either[HttpStatus, Option[OpiskeluOikeus]] = find(OpiskeluOikeusIdentifier(oppijaOid.oppijaOid, opiskeluOikeus))

    opiskeluoikeudet.right.flatMap {
      case Some(oikeus) => update(oppijaOid.oppijaOid, opiskeluOikeus.copy(id = oikeus.id)) match {
        case error if error.isError => Left(error)
        case _ => Right(Updated(oikeus.id.get))
      }
      case _ =>
        oppijaOid.verifiedOid match {
          case Some(oid) => create(oid, opiskeluOikeus).right.map(Created(_))
          case None => Left(HttpStatus.notFound("Oppija " + oppijaOid.oppijaOid + " not found"))
        }
    }
  }

  override def find(identifier: OpiskeluOikeusIdentifier)(implicit userContext: TorUser): Either[HttpStatus, Option[OpiskeluOikeus]] = identifier match{
    case PrimaryKey(id) => find(OpiskeluOikeudet.filter(_.id === id)).headOption match {
      case Some(oikeus) => Right(Some(oikeus))
      case None => Left(HttpStatus.notFound("Opiskeluoikeus not found for id: " + id))
    }
    case IdentifyingSetOfFields(oppijaOid, _, _, _) => {
      Right(findByOppijaOid(oppijaOid).find({
        new IdentifyingSetOfFields(oppijaOid, _) == identifier
      }))
    }
  }

  override def update(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus) = {
    // TODO: always overriding existing data can not be the eventual update strategy
    val rowsUpdated: Int = await(db.run(OpiskeluOikeudet.filter(_.id === opiskeluOikeus.id.get).map(_.data).update(new OpiskeluOikeusRow(oppijaOid, opiskeluOikeus).data)))
    rowsUpdated match {
      case 1 => HttpStatus.ok
      case x =>
        logger.error("Unexpected number of updated rows: " + x)
        HttpStatus.internalError()
    }
  }

  override def query(filters: List[QueryFilter])(implicit userContext: TorUser): Observable[(Oid, List[OpiskeluOikeus])] = {
    import ReactiveStreamsToRx._

    val query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = queryWithAccessCheck(filters.foldLeft(OpiskeluOikeudet.asInstanceOf[Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq]]) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, TutkinnonTila(tila)) => query.filter(_.data.#>>(List("suoritus", "tila", "koodiarvo")) === tila)
    }).sortBy(_.oppijaOid)

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

  private def find(query: Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq])(implicit userContext: TorUser): Seq[OpiskeluOikeus] = {
    runQuery(queryWithAccessCheck(query)).map(_.toOpiskeluOikeus)
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    await(db.run(fullQuery.result))
  }

  def queryWithAccessCheck(query: PostgresDriverWithJsonSupport.api.Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq])(implicit userContext: TorUser): Query[OpiskeluOikeusTable, OpiskeluOikeusRow, Seq] = {
    val oids = userContext.userOrganisations.oids
    val queryWithAccessCheck = for (
      oo <- query
      if oo.data.#>>(List("oppilaitos", "oid")) inSetBind oids)
    yield {
      oo
    }
    queryWithAccessCheck
  }
}


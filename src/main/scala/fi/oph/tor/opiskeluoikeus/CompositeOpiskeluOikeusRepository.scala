package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{Opiskeluoikeus, TaydellisetHenkilötiedot}
import fi.oph.tor.tor.QueryFilter
import fi.oph.tor.toruser.TorUser
import rx.lang.scala.Observable

import scala.collection.parallel.immutable.ParSeq

class CompositeOpiskeluOikeusRepository(repos: List[OpiskeluOikeusRepository]) extends OpiskeluOikeusRepository {
  override def query(filters: List[QueryFilter])(implicit user: TorUser) = repos.foldLeft(Observable.empty.asInstanceOf[Observable[(Oid, List[Opiskeluoikeus])]]) { (result, repo) => result.merge(repo.query(filters)) }

  override def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: TorUser) = {
    repos.foldLeft((oppijat, Nil: Seq[TaydellisetHenkilötiedot])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: TorUser) = repos.par.flatMap(_.findById(id)).headOption

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser) = {
    val results: ParSeq[Either[HttpStatus, CreateOrUpdateResult]] = repos.par.map(_.createOrUpdate(oppijaOid, opiskeluOikeus))
    results.toList.sortWith {
      case (Right(_), Left(_)) => true // prefer success
      case (Left(x), Left(y)) => x.statusCode < y.statusCode
      case _ => false
    }.head
  }

  override def findByOppijaOid(oid: String)(implicit user: TorUser) = repos.par.flatMap(_.findByOppijaOid(oid)).toList
}

package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koski.QueryFilter
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.oppija.PossiblyUnverifiedOppijaOid
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, TaydellisetHenkilötiedot}
import rx.lang.scala.Observable

import scala.collection.parallel.immutable.ParSeq

class CompositeOpiskeluOikeusRepository(repos: List[OpiskeluOikeusRepository]) extends OpiskeluOikeusRepository {
  override def query(filters: List[QueryFilter])(implicit user: KoskiUser) = repos.foldLeft(Observable.empty.asInstanceOf[Observable[(Oid, List[Opiskeluoikeus])]]) { (result, repo) => result.merge(repo.query(filters)) }

  override def filterOppijat(oppijat: Seq[TaydellisetHenkilötiedot])(implicit user: KoskiUser) = {
    repos.foldLeft((oppijat, Nil: Seq[TaydellisetHenkilötiedot])) { case ((left, found), repo) =>
      val newlyFound = repo.filterOppijat(left)
      val stillLeft = left.diff(newlyFound)
      (stillLeft, found ++ newlyFound)
    }._2
  }

  override def findById(id: Int)(implicit user: KoskiUser) = repos.par.flatMap(_.findById(id)).headOption

  override def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiUser) = {
    val results: ParSeq[Either[HttpStatus, CreateOrUpdateResult]] = repos.par.map(_.createOrUpdate(oppijaOid, opiskeluOikeus))
    results.toList.sortWith {
      case (Right(_), Left(_)) => true // prefer success
      case (Left(x), Left(y)) => x.statusCode < y.statusCode
      case _ => false
    }.head
  }

  override def findByOppijaOid(oid: String)(implicit user: KoskiUser) = repos.par.flatMap(_.findByOppijaOid(oid)).toList
}

package fi.oph.tor.oppija

import fi.oph.tor.henkilo.HenkiloOid
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema._

import scala.collection.parallel.immutable.ParSeq

case class CompositeOppijaRepository(repos: List[OppijaRepository]) extends OppijaRepository {
  override def findOppijat(query: String) = {
    repos.iterator.map(_.findOppijat(query)).find(!_.isEmpty).getOrElse(Nil)
  }

  override def findByOid(oid: String) = mergeDuplicates(repos.par.map(_.findByOid(oid).toList).toList).headOption

  override def findOrCreate(henkilö: UusiHenkilö) = {
    val results: ParSeq[Either[HttpStatus, Henkilö.Oid]] = repos.par.map(_.findOrCreate(henkilö))
    results.toList.sortWith {
      case (Right(_), Left(_)) => true // prefer success
      case (Left(x), Left(y)) => x.statusCode < y.statusCode
      case _ => false
    }.head
  }

  override def findByOids(oids: List[String]) = mergeDuplicates(repos.par.map(_.findByOids(oids)).toList)

  private def mergeDuplicates(oppijat: Iterable[Iterable[TaydellisetHenkilötiedot]]): List[TaydellisetHenkilötiedot] = {
    val grouped = oppijat.flatten.toList.groupBy(_.hetu).values
    grouped.flatMap { duplicates =>
      // de-duplicate the ones with nonempty hetu
      duplicates.head.hetu match {
        case "" => duplicates
        case _ => duplicates.sortBy(henkilo => !HenkiloOid.isValidHenkilöOid(henkilo.oid)).headOption
      }
    }.toList
  }
}

package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.cache.{BaseCacheDetails, CachingProxy, CachingStrategyBase}
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.henkilo.{HenkiloOid, AuthenticationServiceClient}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.{Logging, TimedProxy}
import fi.oph.tor.schema._
import fi.oph.tor.util.Invocation
import fi.oph.tor.virta.{VirtaClient, VirtaOppijaRepository, VirtaOpiskeluoikeusRepository}
import scala.collection.parallel.immutable.ParSeq

object OppijaRepository {
  def apply(config: Config, database: TorDatabase, koodistoViitePalvelu: KoodistoViitePalvelu) = {
    CachingProxy(new OppijaRepositoryCachingStrategy, TimedProxy(withoutCache(config, database, koodistoViitePalvelu)))
  }

  def withoutCache(config: Config, database: TorDatabase, koodistoViitePalvelu: KoodistoViitePalvelu): OppijaRepository = {
    CompositeOppijaRepository(List(
      opintopolkuOppijaRepository(config, database, koodistoViitePalvelu),
      virtaOppijaRepository(config)
    ))
  }

  def virtaOppijaRepository(config: Config) = VirtaOppijaRepository(VirtaClient(config))

  def opintopolkuOppijaRepository(config: Config, database: TorDatabase, koodistoViitePalvelu: KoodistoViitePalvelu): OppijaRepository = {
    if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteOppijaRepository(AuthenticationServiceClient(config), koodistoViitePalvelu)
    } else {
      new MockOppijaRepository(Some(database.db))
    }
  }
}

class OppijaRepositoryCachingStrategy extends CachingStrategyBase(new BaseCacheDetails(durationSeconds = 60, maxSize = 100, refreshing = true) {
  override def storeValuePredicate: (Invocation, AnyRef) => Boolean = {
    case (invocation, value) => invocation.method.getName match {
      case "findByOid" => value match {
        case Some(_) => true
        case _ => false
      }
      case _ => false
    }
  }})

trait OppijaRepository extends Logging {
  def findOppijat(query: String): List[TaydellisetHenkilötiedot]
  def findByOid(oid: String): Option[TaydellisetHenkilötiedot]
  def findByOids(oids: List[String]): List[TaydellisetHenkilötiedot]

  def resetFixtures {}

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid]
}

case class CompositeOppijaRepository(repos: List[OppijaRepository]) extends OppijaRepository {
  override def findOppijat(query: String) = mergeDuplicates(repos.par.map(_.findOppijat(query)).toList)

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
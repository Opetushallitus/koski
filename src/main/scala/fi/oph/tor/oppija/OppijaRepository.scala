package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.cache.{BaseCacheDetails, CachingProxy, CachingStrategyBase}
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.{Logging, TimedProxy}
import fi.oph.tor.schema._
import fi.oph.tor.util.Invocation
import fi.oph.tor.virta.{VirtaClient, VirtaOppijaRepository}

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


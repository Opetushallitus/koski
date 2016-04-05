package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.cache.{BaseCacheDetails, CachingStrategyBase, CachingProxy}
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.henkilo.{Hetu, AuthenticationServiceClient}
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.TimedProxy
import fi.oph.tor.schema._
import fi.oph.tor.util.Invocation
import fi.oph.tor.log.Logging

object OppijaRepository {
  def apply(config: Config, database: TorDatabase, koodistoViitePalvelu: KoodistoViitePalvelu) = {
    CachingProxy(new OppijaRepositoryCachingStrategy, TimedProxy(if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteOppijaRepository(AuthenticationServiceClient(config), koodistoViitePalvelu)
    } else {
      new MockOppijaRepository(Some(database.db))
    }))
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
  def findOppijat(query: String): List[FullHenkilö]
  def findByOid(id: String): Option[FullHenkilö]
  def findByOids(oids: List[String]): List[FullHenkilö]

  def resetFixtures {}

  def findOrCreate(henkilö: NewHenkilö): Either[HttpStatus, Henkilö.Oid]
}
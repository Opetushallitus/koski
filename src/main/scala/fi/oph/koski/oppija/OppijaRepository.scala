package fi.oph.koski.oppija

import com.typesafe.config.Config
import fi.oph.koski.cache.{CacheDetails, CachingProxy, CachingStrategy}
import fi.oph.koski.db.KoskiDatabase
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.schema._
import fi.oph.koski.util.Invocation
import fi.oph.koski.virta.{VirtaClient, VirtaOppijaRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrOppijaRepository}

object OppijaRepository {
  def apply(config: Config, database: KoskiDatabase, koodistoViitePalvelu: KoodistoViitePalvelu, virtaClient: VirtaClient, ytr: YlioppilasTutkintoRekisteri) = {
    CachingProxy(OppijaRepositoryCachingStrategy, TimedProxy(withoutCache(config, database, koodistoViitePalvelu, virtaClient, ytr)))
  }

  def withoutCache(config: Config, database: KoskiDatabase, koodistoViitePalvelu: KoodistoViitePalvelu, virtaClient: VirtaClient, ytr: YlioppilasTutkintoRekisteri): OppijaRepository = {
    val opintopolku = opintopolkuOppijaRepository(config, database, koodistoViitePalvelu)
    CompositeOppijaRepository(List(
      TimedProxy(opintopolku),
      TimedProxy(VirtaOppijaRepository(virtaClient, opintopolku).asInstanceOf[OppijaRepository]),
      TimedProxy(YtrOppijaRepository(ytr, opintopolku).asInstanceOf[OppijaRepository])
    ))
  }

  def opintopolkuOppijaRepository(config: Config, database: KoskiDatabase, koodistoViitePalvelu: KoodistoViitePalvelu): OppijaRepository = {
    if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteOppijaRepository(AuthenticationServiceClient(config), koodistoViitePalvelu)
    } else {
      new MockOppijaRepository(MockOppijat.defaultOppijat, Some(database.db))
    }
  }
}

object OppijaRepositoryCachingStrategy extends CachingStrategy("OppijaRepository", new CacheDetails {
  def durationSeconds = 60
  def maxSize = 100
  def refreshing = true
  override def storeValuePredicate: (Invocation, AnyRef) => Boolean = {
    case (invocation, value) => invocation.f.name match {
      case "findByOid" => value match {
        case Some(_) => true
        case _ => false
      }
      case _ => false
    }
  }})

trait OppijaRepository extends Logging {
  def findOppijat(query: String): List[HenkilötiedotJaOid]
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot]
  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot]

  def resetFixtures {}

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid]
}


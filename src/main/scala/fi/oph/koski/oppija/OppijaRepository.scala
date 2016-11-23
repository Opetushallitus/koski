package fi.oph.koski.oppija

import fi.oph.koski.cache._
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.TimedProxy
import fi.oph.koski.schema._
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOppijaRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrAccessChecker, YtrOppijaRepository}

trait OppijaRepository extends AuxiliaryOppijaRepository {
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot]
  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot]
  def resetFixtures {}
  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid]
  def findOppijat(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid]
}

trait AuxiliaryOppijaRepository {
  def findOppijat(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid]
}

object OppijaRepository {
  def apply(authenticationServiceClient: AuthenticationServiceClient, koodistoViitePalvelu: KoodistoViitePalvelu, virtaClient: VirtaClient, virtaAccessChecker: VirtaAccessChecker, ytr: YlioppilasTutkintoRekisteri, ytrAccessChecker: YtrAccessChecker)(implicit cacheInvalidator: CacheManager) = {
    val opintopolku = new OpintopolkuOppijaRepository(authenticationServiceClient, koodistoViitePalvelu)
    CachingOppijaRepository(TimedProxy(
      CompositeOppijaRepository(
        TimedProxy(opintopolku.asInstanceOf[OppijaRepository]),
        List(
          TimedProxy(VirtaOppijaRepository(virtaClient, opintopolku, virtaAccessChecker).asInstanceOf[AuxiliaryOppijaRepository]),
          TimedProxy(YtrOppijaRepository(ytr, opintopolku, ytrAccessChecker).asInstanceOf[AuxiliaryOppijaRepository])
      )).asInstanceOf[OppijaRepository]
    ))
  }
}

case class CachingOppijaRepository(repository: OppijaRepository)(implicit cacheInvalidator: CacheManager) extends OppijaRepository {
  private val oidCache = KeyValueCache(Cache.cacheAllNoRefresh("OppijaRepository", 3600, 100), repository.findByOid)
  // findByOid is locally cached
  override def findByOid(oid: String) = oidCache(oid)
  // Other methods just call the non-cached implementation
  override def findByOids(oids: List[String]) = repository.findByOids(oids)
  override def findOrCreate(henkilö: UusiHenkilö) = repository.findOrCreate(henkilö)
  override def findOppijat(query: String)(implicit user: KoskiSession) = repository.findOppijat(query)
}
package fi.oph.koski.henkilo

import fi.oph.koski.cache._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.TimedProxy
import fi.oph.koski.schema._
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaHenkilöRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrAccessChecker, YtrHenkilöRepository}

trait HenkilöRepository extends AuxiliaryHenkilöRepository {
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot]
  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot]
  def resetFixtures {}
  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid]
  def findOppijat(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid]
}

trait AuxiliaryHenkilöRepository {
  def findOppijat(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid]
}

object HenkilöRepository {
  def apply(authenticationServiceClient: AuthenticationServiceClient, koodistoViitePalvelu: KoodistoViitePalvelu, virtaClient: VirtaClient, virtaAccessChecker: VirtaAccessChecker, ytr: YlioppilasTutkintoRekisteri, ytrAccessChecker: YtrAccessChecker)(implicit cacheInvalidator: CacheManager) = {
    val opintopolku = new OpintopolkuHenkilöRepository(authenticationServiceClient, koodistoViitePalvelu)
    CachingHenkilöRepository(TimedProxy(
      CompositeHenkilöRepository(
        TimedProxy(opintopolku.asInstanceOf[HenkilöRepository]),
        List(
          TimedProxy(VirtaHenkilöRepository(virtaClient, opintopolku, virtaAccessChecker).asInstanceOf[AuxiliaryHenkilöRepository]),
          TimedProxy(YtrHenkilöRepository(ytr, opintopolku, ytrAccessChecker).asInstanceOf[AuxiliaryHenkilöRepository])
      )).asInstanceOf[HenkilöRepository]
    ))
  }
}

case class CachingHenkilöRepository(repository: HenkilöRepository)(implicit cacheInvalidator: CacheManager) extends HenkilöRepository {
  private val oidCache = KeyValueCache(Cache.cacheAllNoRefresh("OppijaRepository", 3600, 100), repository.findByOid)
  // findByOid is locally cached
  override def findByOid(oid: String) = oidCache(oid)
  // Other methods just call the non-cached implementation
  override def findByOids(oids: List[String]) = repository.findByOids(oids)
  override def findOrCreate(henkilö: UusiHenkilö) = repository.findOrCreate(henkilö)
  override def findOppijat(query: String)(implicit user: KoskiSession) = repository.findOppijat(query)
}
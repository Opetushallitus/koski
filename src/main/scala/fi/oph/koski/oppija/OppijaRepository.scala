package fi.oph.koski.oppija

import fi.oph.koski.cache.{CacheDetails, CachingProxy, CachingStrategy}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.TimedProxy
import fi.oph.koski.schema._
import fi.oph.koski.util.Invocation
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOppijaRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrAccessChecker, YtrOppijaRepository}

trait OppijaRepository extends AuxiliaryOppijaRepository {
  def findByOid(oid: String)(implicit user: KoskiUser): Option[TäydellisetHenkilötiedot]
  def findByOids(oids: List[String])(implicit user: KoskiUser): List[TäydellisetHenkilötiedot]
  def resetFixtures {}
  def findOrCreate(henkilö: UusiHenkilö)(implicit user: KoskiUser): Either[HttpStatus, Henkilö.Oid]
  def findOppijat(query: String)(implicit user: KoskiUser): List[HenkilötiedotJaOid]
}

trait AuxiliaryOppijaRepository {
  def findOppijat(query: String)(implicit user: KoskiUser): List[HenkilötiedotJaOid]
}

object OppijaRepository {
  def apply(authenticationServiceClient: AuthenticationServiceClient, koodistoViitePalvelu: KoodistoViitePalvelu, virtaClient: VirtaClient, virtaAccessChecker: VirtaAccessChecker, ytr: YlioppilasTutkintoRekisteri, ytrAccessChecker: YtrAccessChecker) = {
    val opintopolku = new OpintopolkuOppijaRepository(authenticationServiceClient, koodistoViitePalvelu)
    CachingProxy(OppijaRepositoryCachingStrategy, TimedProxy(
      CompositeOppijaRepository(
        TimedProxy(opintopolku.asInstanceOf[OppijaRepository]),
        List(
          TimedProxy(VirtaOppijaRepository(virtaClient, opintopolku, virtaAccessChecker).asInstanceOf[AuxiliaryOppijaRepository]),
          TimedProxy(YtrOppijaRepository(ytr, opintopolku, ytrAccessChecker).asInstanceOf[AuxiliaryOppijaRepository])
      )).asInstanceOf[OppijaRepository]
    ))
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

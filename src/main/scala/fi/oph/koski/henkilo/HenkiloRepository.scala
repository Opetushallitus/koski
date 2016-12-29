package fi.oph.koski.henkilo

import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.TimedProxy
import fi.oph.koski.schema._
import fi.oph.koski.virta.VirtaHenkilöRepository
import fi.oph.koski.ytr.YtrHenkilöRepository

trait FindByOid {
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot]
}

trait FindByHetu {
  def findByHetu(query: String)(implicit user: KoskiSession): Option[HenkilötiedotJaOid]
}

object HenkilöRepository {
  def apply(application: KoskiApplication)(implicit cacheInvalidator: CacheManager): HenkilöRepository = {
    val opintopolku = new OpintopolkuHenkilöRepository(application.authenticationServiceClient, application.koodistoViitePalvelu)
    HenkilöRepository(
      opintopolku,
      TimedProxy(VirtaHenkilöRepository(application.virtaClient, opintopolku, application.virtaAccessChecker).asInstanceOf[FindByHetu]),
      TimedProxy(YtrHenkilöRepository(application.ytrClient, opintopolku, application.ytrAccessChecker).asInstanceOf[FindByHetu]),
      application.henkilöCache
    )
  }
}

case class HenkilöRepository(opintopolku: OpintopolkuHenkilöRepository, virta: FindByHetu, ytr: FindByHetu, henkilöCache: KoskiHenkilöCache)(implicit cacheInvalidator: CacheManager) extends FindByOid {
  private val oidCache = KeyValueCache(Cache.cacheAllNoRefresh("HenkilöRepository", 3600, 100), opintopolku.findByOid)
  // findByOid is locally cached
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = oidCache(oid)
  // Other methods just call the non-cached implementation

  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = opintopolku.findByOids(oids)

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] = opintopolku.findOrCreate(henkilö)

  def findOppijat(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    if (Henkilö.isHenkilöOid(query)) {
      findByOid(query).map(_.toHenkilötiedotJaOid).toList
    } else if(Hetu.validFormat(query).isRight) {
      List(opintopolku, virta, ytr).iterator.map(_.findByHetu(query)).find(!_.isEmpty).toList.flatten
    } else {
      val oids = henkilöCache.findOids(query)
      findByOids(oids).map(_.toHenkilötiedotJaOid)
    }
  }
}
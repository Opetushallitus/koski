package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.schema.HenkilöWithOid

import scala.concurrent.duration._

class KoskiUserRepository(henkilöt: OpintopolkuHenkilöFacade)(implicit cacheManager: CacheManager) {
  private val oidCache = KeyValueCache(ExpiringCache("KoskiUserRepository", 1.hour, 15000), { oid: String =>
    henkilöt.findKäyttäjäByOid(oid).map { henkilö =>
      KoskiUserInfo(henkilö.oidHenkilo, henkilö.kayttajatiedot.flatMap(_.username), Some(henkilö.etunimet + " " + henkilö.sukunimi))
    }
  })

  def findByOid(oid: String): Option[KoskiUserInfo] = oidCache(oid)
}

case class KoskiUserInfo(oid: String, käyttäjätunnus: Option[String], kokonimi: Option[String]) extends HenkilöWithOid
package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CacheManager, KeyValueCache, KoskiCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.schema.HenkilöWithOid

class KoskiUserRepository(client: AuthenticationServiceClient)(implicit cacheManager: CacheManager) {
  private val oidCache = KeyValueCache(KoskiCache.cacheStrategy("KoskiUserRepository"), { oid: String =>
    client.findKäyttäjäByOid(oid).map { henkilö =>
      KoskiUserInfo(henkilö.oidHenkilo, henkilö.kayttajatiedot.flatMap(_.username), Some(henkilö.etunimet + " " + henkilö.sukunimi))
    }
  })

  def findByOid(oid: String): Option[KoskiUserInfo] = oidCache(oid)
}

case class KoskiUserInfo(oid: String, käyttäjätunnus: Option[String], kokonimi: Option[String]) extends HenkilöWithOid
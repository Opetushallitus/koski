package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CacheManager, KeyValueCache, KoskiCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.schema.{HenkilöWithOid, NimellinenHenkilö}

class KoskiUserRepository(client: AuthenticationServiceClient)(implicit cacheManager: CacheManager) {
  private val oidCache = KeyValueCache(KoskiCache.cacheStrategy("KoskiUserRepository"), { oid: String =>
    client.findByOid(oid).map { henkilö =>
      KoskiUserInfo(henkilö.oidHenkilo, henkilö.kayttajatiedot.flatMap(_.username), henkilö.etunimet + " " + henkilö.sukunimi)
    }
  })

  def findByOid(oid: String): Option[KoskiUserInfo] = oidCache(oid)
}

case class KoskiUserInfo(oid: String, käyttäjätunnus: Option[String], kokonimi: String) extends HenkilöWithOid
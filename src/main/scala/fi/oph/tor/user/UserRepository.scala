package fi.oph.tor.user

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository, RemoteOrganisaatioRepository}
import fi.oph.tor.util.TimedProxy

object UserRepository {
  def apply(config: Config): UserRepository = {
    if (config.hasPath("authentication-service")) {
      CachingProxy(TorCache.cacheStrategy, TimedProxy[UserRepository](new RemoteUserRepository(AuthenticationServiceClient(config), TimedProxy[OrganisaatioRepository](new RemoteOrganisaatioRepository(config)))))
    } else {
      new MockUserRepository
    }
  }
}

trait UserRepository {
  def getUserOrganisations(oid: String): OrganisaatioPuu
}
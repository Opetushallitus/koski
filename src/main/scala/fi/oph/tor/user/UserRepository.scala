package fi.oph.tor.user

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository, RemoteOrganisaatioRepository}
import fi.oph.tor.util.{CachingProxy, TimedProxy}

object UserRepository {
  def apply(config: Config): UserRepository = {
    if (config.hasPath("authentication-service")) {
      CachingProxy(config, TimedProxy[UserRepository](new RemoteUserRepository(HenkilöPalveluClient(config), TimedProxy[OrganisaatioRepository](new RemoteOrganisaatioRepository(config)))))
    } else {
      new MockUserRepository
    }
  }
}

trait UserRepository {
  def getUserOrganisations(oid: String): OrganisaatioPuu
}
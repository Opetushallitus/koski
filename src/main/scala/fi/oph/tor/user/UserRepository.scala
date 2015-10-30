package fi.oph.tor.user

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.organisaatio.{RemoteOrganisaatioRepository, OrganisaatioPuu, OrganisaatioRepository}
import fi.oph.tor.util.Timed._

object UserRepository {
  def apply(config: Config): UserRepository = {
    if (config.hasPath("authentication-service")) {
      new CachingUserRepository(timedProxy[UserRepository](new RemoteUserRepository(HenkilöPalveluClient(config), timedProxy[OrganisaatioRepository](new RemoteOrganisaatioRepository(config)))))
    } else {
      new MockUserRepository
    }
  }
}

trait UserRepository {
  def getUserOrganisations(oid: String): OrganisaatioPuu
}
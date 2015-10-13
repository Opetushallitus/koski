package fi.oph.tor.user

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository}

object UserRepository {
  def apply(config: Config): UserRepository = {
    if (config.hasPath("authentication-service")) {
      new CachingUserRepository(new RemoteUserRepository(HenkilöPalveluClient(config), new OrganisaatioRepository(config)))
    } else {
      new MockUserRepository
    }
  }
}

trait UserRepository {
  def getUserOrganisations(oid: String): OrganisaatioPuu
}
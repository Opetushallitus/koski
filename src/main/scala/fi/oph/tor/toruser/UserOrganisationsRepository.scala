package fi.oph.tor.toruser

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.organisaatio.{InMemoryOrganisaatioRepository, OrganisaatioRepository}
import fi.oph.tor.util.TimedProxy

object UserOrganisationsRepository {
  def apply(config: Config, organisaatioRepository: OrganisaatioRepository): UserOrganisationsRepository = {
    CachingProxy(TorCache.cacheStrategy, TimedProxy[UserOrganisationsRepository](if (config.hasPath("authentication-service")) {
      new RemoteUserOrganisationsRepository(AuthenticationServiceClient(config), organisaatioRepository)
    } else {
      new MockUserOrganisationsRepository
    }))
  }
}

trait UserOrganisationsRepository {
  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository
}

import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.organisaatio.OrganisaatioRepository
import org.http4s.EntityDecoderInstances

object RemoteUserOrganisationsRepository {
  val käyttöoikeusryhmä = 4056292
}
class RemoteUserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository) extends UserOrganisationsRepository with EntityDecoderInstances {
  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository = {
    new InMemoryOrganisaatioRepository(
      henkilöPalveluClient.organisaatiot(oid)
        .withFilter {!_.passivoitu}
        .flatMap {org => henkilöPalveluClient.käyttöoikeusryhmät(oid, org.organisaatioOid)}
        .withFilter {_.ryhmaId == RemoteUserOrganisationsRepository.käyttöoikeusryhmä}
        .withFilter {o => o.tila == "MYONNETTY" || o.tila == "UUSITTU"}
        .withFilter {_.effective}
        .flatMap {result => organisaatioRepository.getOrganisaatio(result.organisaatioOid)}
    )
  }
}
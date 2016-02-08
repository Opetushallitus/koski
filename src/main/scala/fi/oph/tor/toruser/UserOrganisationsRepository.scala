package fi.oph.tor.toruser

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.log.TimedProxy
import fi.oph.tor.organisaatio.{InMemoryOrganisaatioRepository, OrganisaatioRepository}
import fi.oph.tor.util.Timer

object UserOrganisationsRepository {
  def apply(config: Config, organisaatioRepository: OrganisaatioRepository): UserOrganisationsRepository = {
    CachingProxy(TorCache.cacheStrategy, TimedProxy[UserOrganisationsRepository](if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteUserOrganisationsRepository(AuthenticationServiceClient(config), organisaatioRepository, KäyttöoikeusRyhmät(config))
    } else {
      MockUsers
    }))
  }
}

trait UserOrganisationsRepository {
  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository
}

import fi.oph.tor.henkilo.AuthenticationServiceClient
import fi.oph.tor.organisaatio.OrganisaatioRepository
import org.http4s.EntityDecoderInstances

class RemoteUserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository, käyttöoikeusRyhmät: KäyttöoikeusRyhmät) extends UserOrganisationsRepository with EntityDecoderInstances {
  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository = {
    new InMemoryOrganisaatioRepository(
      Timer.timed("Fetch user's organisations", 0)(henkilöPalveluClient.organisaatiot(oid, käyttöoikeusRyhmät.readWrite))
        .flatMap {organisaatioOid => organisaatioRepository.getOrganisaatioHierarkia(organisaatioOid)}
    )
  }
}


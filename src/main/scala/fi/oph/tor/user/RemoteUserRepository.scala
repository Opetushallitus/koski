package fi.oph.tor.user

import fi.oph.tor.henkilö.AuthenticationServiceClient
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository}
import org.http4s.EntityDecoderInstances

object RemoteUserRepository {
  val käyttöoikeusryhmä = 4056292
}
class RemoteUserRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository) extends UserRepository with EntityDecoderInstances {
  def getUserOrganisations(oid: String): OrganisaatioPuu = {
    OrganisaatioPuu(
      roots = henkilöPalveluClient.organisaatiot(oid)
        .withFilter {!_.passivoitu}
        .flatMap {org => henkilöPalveluClient.käyttöoikeusryhmät(oid, org.organisaatioOid)}
        .withFilter {_.ryhmaId == RemoteUserRepository.käyttöoikeusryhmä}
        .withFilter {o => o.tila == "MYONNETTY" || o.tila == "UUSITTU"}
        .withFilter {_.effective}
        .flatMap {result => organisaatioRepository.getOrganisaatio(result.organisaatioOid)}
    )
  }
}
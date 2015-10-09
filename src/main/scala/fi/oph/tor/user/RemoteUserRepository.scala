package fi.oph.tor.user

import fi.oph.tor.http.VirkailijaHttpClient
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository, Organisaatio}
import org.http4s.{EntityDecoderInstances, Request}

/*
    TODO: pitääkö näillä filtteröidä?

    "passivoitu": false,
    "voimassaAlkuPvm": null,
    "voimassaLoppuPvm": null
 */


class RemoteUserRepository(henkilöPalveluClient: VirkailijaHttpClient, organisaatioRepository: OrganisaatioRepository) extends UserRepository with EntityDecoderInstances {
  def getUserOrganisations(oid: String): OrganisaatioPuu = OrganisaatioPuu(henkilöPalveluClient.httpClient
    .prepAs[List[OrganisaatioHenkilö]](Request(uri = henkilöPalveluClient.uriFromString("/authentication-service/resources/henkilo/" + oid + "/organisaatiohenkilo")))(json4sOf[List[OrganisaatioHenkilö]])
    .run.flatMap { result => organisaatioRepository.getOrganisaatio(result.organisaatioOid)})

}

case class OrganisaatioHenkilö(organisaatioOid: String)

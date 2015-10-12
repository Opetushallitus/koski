package fi.oph.tor.user

import java.time.{LocalDate, ZoneId}

import fi.oph.tor.http.VirkailijaHttpClient
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository}
import org.http4s.{EntityDecoderInstances, Request}

class RemoteUserRepository(henkilöPalveluClient: VirkailijaHttpClient, organisaatioRepository: OrganisaatioRepository) extends UserRepository with EntityDecoderInstances {
  val katselijaRole = 176260L

  def getUserOrganisations(oid: String): OrganisaatioPuu = OrganisaatioPuu(
    roots = henkilöPalveluClient.httpClient
    .prepAs[List[OrganisaatioHenkilö]](Request(uri = henkilöPalveluClient.uriFromString(s"/authentication-service/resources/henkilo/${oid}/organisaatiohenkilo")))(json4sOf[List[OrganisaatioHenkilö]])
    .run
    .withFilter {!_.passivoitu}
    .flatMap {org => getKäyttöoikeudet(oid, org.organisaatioOid)}
    .withFilter {_.ryhmaId == katselijaRole}
    .withFilter {o => o.tila == "MYONNETTY" || o.tila == "UUSITTU"}
    .withFilter {_.effective}
    .flatMap {result => organisaatioRepository.getOrganisaatio(result.organisaatioOid)}
  )

  private def getKäyttöoikeudet(oid: String, ooid: String): List[Käyttöoikeus] = {
    henkilöPalveluClient.httpClient
      .prepAs[List[Käyttöoikeus]](
        Request(
          uri = henkilöPalveluClient.uriFromString(s"/authentication-service/resources/kayttooikeusryhma/henkilo/${oid}?ooid=${ooid}")
        ))(json4sOf[List[Käyttöoikeus]])
      .run
  }
}

case class OrganisaatioHenkilö(organisaatioOid: String, passivoitu: Boolean)
case class Käyttöoikeus(ryhmaId: Long, organisaatioOid: String, tila: String, alkuPvm: LocalDate, voimassaPvm: LocalDate) {
  def effective = {
      val now: LocalDate = LocalDate.now(ZoneId.of("UTC"))
      !now.isBefore(alkuPvm) && !now.isAfter(voimassaPvm)
  }
}

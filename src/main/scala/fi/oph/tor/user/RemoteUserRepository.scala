package fi.oph.tor.user

import java.time.{LocalDate, ZoneId}

import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.oph.tor.organisaatio.{OrganisaatioPuu, OrganisaatioRepository}
import org.http4s.{EntityDecoderInstances, Request}

import scalaz.concurrent.Task

class RemoteUserRepository(henkilöPalveluClient: VirkailijaHttpClient, organisaatioRepository: OrganisaatioRepository) extends UserRepository with EntityDecoderInstances {
  val katselijaRole = 176260L

  def getUserOrganisations(oid: String): OrganisaatioPuu = OrganisaatioPuu(
    roots = henkilöPalveluClient.httpClient
      .apply(Task(Request(uri = henkilöPalveluClient.uriFromString(s"/authentication-service/resources/henkilo/${oid}/organisaatiohenkilo"))))(Http.parseJson[List[OrganisaatioHenkilö]])
      .withFilter {!_.passivoitu}
      .flatMap {org => getKäyttöoikeudet(oid, org.organisaatioOid)}
      .withFilter {_.ryhmaId == katselijaRole}
      .withFilter {o => o.tila == "MYONNETTY" || o.tila == "UUSITTU"}
      .withFilter {_.effective}
      .flatMap {result => organisaatioRepository.getOrganisaatio(result.organisaatioOid)}
  )

  private def getKäyttöoikeudet(oid: String, ooid: String): List[Käyttöoikeus] = {
    val request = Request(uri = henkilöPalveluClient.uriFromString(s"/authentication-service/resources/kayttooikeusryhma/henkilo/${oid}?ooid=${ooid}"))

    henkilöPalveluClient.httpClient(Task(request))(Http.parseJson[List[Käyttöoikeus]])
  }
}

case class OrganisaatioHenkilö(organisaatioOid: String, passivoitu: Boolean)
case class Käyttöoikeus(ryhmaId: Long, organisaatioOid: String, tila: String, alkuPvm: LocalDate, voimassaPvm: LocalDate) {
  def effective = {
      val now: LocalDate = LocalDate.now(ZoneId.of("UTC"))
      !now.isBefore(alkuPvm) && !now.isAfter(voimassaPvm)
  }
}

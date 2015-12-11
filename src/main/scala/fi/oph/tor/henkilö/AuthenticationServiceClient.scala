package fi.oph.tor.henkilö

import java.time.{LocalDate, ZoneId}

import com.typesafe.config.Config
import fi.oph.tor.http.{Http, HttpStatus, VirkailijaHttpClient}
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import org.http4s.{EntityDecoderInstances, Method, Request}

import scalaz.concurrent.Task

class AuthenticationServiceClient(virkailija: VirkailijaHttpClient) extends EntityDecoderInstances {
  def search(query: String): AuthenticationServiceUserQueryResult = virkailija.httpClient(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo?no=true&count=0&q=" + query))(Http.parseJson[AuthenticationServiceUserQueryResult])
  def findByOid(id: String): Option[AuthenticationServiceUser] = virkailija.httpClient(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo/" + id))(Http.parseJsonOptional[AuthenticationServiceUser])
  def organisaatiot(oid: String): List[OrganisaatioHenkilö] = virkailija.httpClient(virkailija.virkailijaUriFromString(s"/authentication-service/resources/henkilo/${oid}/organisaatiohenkilo"))(Http.parseJson[List[OrganisaatioHenkilö]])
  def käyttöoikeusryhmät(henkilöOid: String, organisaatioOid: String): List[Käyttöoikeusryhmä] = virkailija.httpClient(virkailija.virkailijaUriFromString(s"/authentication-service/resources/kayttooikeusryhma/henkilo/${henkilöOid}?ooid=${organisaatioOid}"))(Http.parseJson[List[Käyttöoikeusryhmä]])
  def lisääKäyttöoikeusRyhmä(henkilö: String, org: String, ryhmä: Int): Unit = {
    virkailija.httpClient.put(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo/" + henkilö + "/organisaatiohenkilo/" + org + "/kayttooikeusryhmat"), List(LisääKäyttöoikeusryhmä(ryhmä)))
  }
  def create(createUserInfo: AuthenticationServiceCreateUser): Either[HttpStatus, String] with Product with Serializable = {
    val task: Task[Request] = Request(
      uri = virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo"),
      method = Method.POST
    ).withBody(createUserInfo)(json4sEncoderOf[AuthenticationServiceCreateUser])

    virkailija.httpClient(task) {
      case (200, oid) => Right(oid)
      case (400, "socialsecuritynr.already.exists") => Left(HttpStatus.conflict("socialsecuritynr.already.exists"))
      case (400, error) => Left(HttpStatus.badRequest(error))
      case (status, text) => throw new RuntimeException(status + ": " + text)
    }
  }
}

object AuthenticationServiceClient {
  def apply(config: Config) = {
    new AuthenticationServiceClient(new VirkailijaHttpClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"), "/authentication-service"))
  }
}


case class AuthenticationServiceUserQueryResult(totalCount: Integer, results: List[AuthenticationServiceUser])
case class AuthenticationServiceUser(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: String)
case class AuthenticationServiceCreateUser(hetu: String, henkiloTyyppi: String, sukunimi: String, etunimet: String, kutsumanimi: String)
case class OrganisaatioHenkilö(organisaatioOid: String, passivoitu: Boolean)
case class Käyttöoikeusryhmä(ryhmaId: Long, organisaatioOid: String, tila: String, alkuPvm: LocalDate, voimassaPvm: LocalDate) {
  def effective = {
    val now: LocalDate = LocalDate.now(ZoneId.of("UTC"))
    !now.isBefore(alkuPvm) && !now.isAfter(voimassaPvm)
  }
}
case class LisääKäyttöoikeusryhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)
package fi.oph.tor.henkilö

import java.time.{LocalDate, ZoneId}

import com.typesafe.config.Config
import fi.oph.tor.http.{Http, HttpStatus, VirkailijaHttpClient}
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import org.http4s._
import org.http4s.headers.`Content-Type`
import org.json4s.jackson.Serialization._
import scalaz.concurrent.Task

class AuthenticationServiceClient(virkailija: VirkailijaHttpClient) extends EntityDecoderInstances {
  def search(query: String): UserQueryResult = virkailija.httpClient(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo?no=true&count=0&q=" + query))(Http.parseJson[UserQueryResult])
  def findByOid(id: String): Option[User] = virkailija.httpClient(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo/" + id))(Http.parseJsonOptional[User])
  def organisaatiot(oid: String): List[OrganisaatioHenkilö] = virkailija.httpClient(virkailija.virkailijaUriFromString(s"/authentication-service/resources/henkilo/${oid}/organisaatiohenkilo"))(Http.parseJson[List[OrganisaatioHenkilö]])
  def käyttöoikeusryhmät(henkilöOid: String, organisaatioOid: String): List[Käyttöoikeusryhmä] = virkailija.httpClient(virkailija.virkailijaUriFromString(s"/authentication-service/resources/kayttooikeusryhma/henkilo/${henkilöOid}?ooid=${organisaatioOid}"))(Http.parseJson[List[Käyttöoikeusryhmä]])
  def lisääOrganisaatio(henkilöOid: String, organisaatioOid: String, nimike: String) = {
    virkailija.httpClient.put(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo/" + henkilöOid + "/organisaatiohenkilo"), List(
      LisääOrganisaatio(organisaatioOid, nimike)
    ))(json4sEncoderOf[List[LisääOrganisaatio]])
  }
  def lisääKäyttöoikeusRyhmä(henkilöOid: String, organisaatioOid: String, ryhmä: Int): Unit = {
    virkailija.httpClient.put(virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo/" + henkilöOid + "/organisaatiohenkilo/" + organisaatioOid + "/kayttooikeusryhmat"), List(LisääKäyttöoikeusryhmä(ryhmä)))(json4sEncoderOf[List[LisääKäyttöoikeusryhmä]])
  }
  def asetaSalasana(henkilöOid: String, salasana: String) = {
    virkailija.httpClient.post (virkailija.virkailijaUriFromString("/authentication-service/resources/salasana/" + henkilöOid), salasana)(EntityEncoder.stringEncoder(Charset.`UTF-8`)
      .withContentType(`Content-Type`(MediaType.`application/json`))) // <- yes, the API expects media type application/json, but consumes inputs as text/plain
  }
  def create(createUserInfo: CreateUser): Either[HttpStatus, String] = {
    val task: Task[Request] = Request(
      uri = virkailija.virkailijaUriFromString("/authentication-service/resources/henkilo"),
      method = Method.POST
    ).withBody(createUserInfo)(json4sEncoderOf[CreateUser])

    virkailija.httpClient(task) {
      case (200, oid) => Right(oid)
      case (400, "socialsecuritynr.already.exists") => Left(HttpStatus.conflict("socialsecuritynr.already.exists"))
      case (400, error) => Left(HttpStatus.badRequest(error))
      case (status, text) => throw new RuntimeException(status + ": " + text)
    }
  }
  def syncLdap(henkilöOid: String) = {
    virkailija.httpClient(virkailija.virkailijaUriFromString("/authentication-service/resources/ldap/" + henkilöOid))(Http.expectSuccess)
  }
}

object AuthenticationServiceClient {
  def apply(config: Config) = {
    new AuthenticationServiceClient(new VirkailijaHttpClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"), "/authentication-service"))
  }
}


case class UserQueryResult(totalCount: Integer, results: List[User])
case class User(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: String)


case class CreateUser(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String, kayttajatiedot: Option[Käyttajatiedot])
case class Käyttajatiedot(username: String)

object CreateUser {
  def palvelu(nimi: String) = CreateUser(None, nimi, "_", "_", "PALVELU", Some(Käyttajatiedot(nimi)))
  def oppija(hetu: String, sukunimi: String, etunimet: String, kutsumanimi: String) = CreateUser(Some(hetu), sukunimi, etunimet, kutsumanimi, "OPPIJA", None)
}


case class OrganisaatioHenkilö(organisaatioOid: String, passivoitu: Boolean)
case class Käyttöoikeusryhmä(ryhmaId: Long, organisaatioOid: String, tila: String, alkuPvm: LocalDate, voimassaPvm: LocalDate) {
  def effective = {
    val now: LocalDate = LocalDate.now(ZoneId.of("UTC"))
    !now.isBefore(alkuPvm) && !now.isAfter(voimassaPvm)
  }
}
case class LisääKäyttöoikeusryhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)

case class LisääOrganisaatio(organisaatioOid: String, tehtavanimike: String, passivoitu: Boolean = false, newOne: Boolean = true)
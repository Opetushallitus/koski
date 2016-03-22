package fi.oph.tor.henkilo

import java.time.{LocalDate, ZoneId}

import com.typesafe.config.Config
import fi.oph.tor.http.Http.runTask
import fi.oph.tor.http._
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import fi.oph.tor.util.ScalazTaskToObservable._
import fi.oph.tor.util.Timing
import org.http4s.Uri.Path
import org.http4s._
import org.http4s.headers.`Content-Type`
import rx.lang.scala.Observable

import scalaz.concurrent.Task

class AuthenticationServiceClient(http: Http) extends EntityDecoderInstances with Timing {
  def search(query: String): UserQueryResult = runTask(http("/authentication-service/resources/henkilo?no=true&count=0&q=" + query)(Http.parseJson[UserQueryResult]))


  def findByOid(id: String): Option[User] = findByOids(List(id)).headOption
  def findByOids(oids: List[String]): List[User] = http.post("/authentication-service/resources/s2s/tor/henkilotByHenkiloOidList", oids)(json4sEncoderOf[List[String]], Http.parseJson[List[User]])
  def käyttäjänOrganisaatiot(oid: String, käyttöoikeusRyhmä: Int): Observable[List[String]] = {
    http(s"/authentication-service/resources/s2s/flatorgs/${oid}/${käyttöoikeusRyhmä}")(Http.parseJson[List[String]])
  }

  def lisääOrganisaatio(henkilöOid: String, organisaatioOid: String, nimike: String) = {
    http.put("/authentication-service/resources/henkilo/" + henkilöOid + "/organisaatiohenkilo", List(
      LisääOrganisaatio(organisaatioOid, nimike)
    ))(json4sEncoderOf[List[LisääOrganisaatio]], Http.unitDecoder)
  }
  def lisääKäyttöoikeusRyhmä(henkilöOid: String, organisaatioOid: String, ryhmä: Int): Unit = {
    http.put(
      "/authentication-service/resources/henkilo/" + henkilöOid + "/organisaatiohenkilo/" + organisaatioOid + "/kayttooikeusryhmat",
      List(LisääKäyttöoikeusryhmä(ryhmä))
    )(json4sEncoderOf[List[LisääKäyttöoikeusryhmä]], Http.unitDecoder)
  }
  def asetaSalasana(henkilöOid: String, salasana: String) = {
    http.post ("/authentication-service/resources/salasana/" + henkilöOid, salasana)(EntityEncoder.stringEncoder(Charset.`UTF-8`)
      .withContentType(`Content-Type`(MediaType.`application/json`)), Http.unitDecoder) // <- yes, the API expects media type application/json, but consumes inputs as text/plain
  }
  def findOrCreate(createUserInfo: CreateUser) = {
    val request: Request = Request(uri = http.uriFromString("/authentication-service/resources/s2s/tor/henkilo"), method = Method.POST)
    runTask(http(request.withBody(createUserInfo)(json4sEncoderOf[CreateUser])) {
      case (200, data, _) => Right(Json.read[User](data))
      case (400, error, _) => Left(TorErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def create(createUserInfo: CreateUser): Either[HttpStatus, String] = {
    val request: Request = Request(uri = http.uriFromString("/authentication-service/resources/henkilo"), method = Method.POST)
    runTask(http(request.withBody(createUserInfo)(json4sEncoderOf[CreateUser])) {
      case (200, oid, _) => Right(oid)
      case (400, "socialsecuritynr.already.exists", _) => Left(TorErrorCategory.conflict.hetu("Henkilötunnus on jo olemassa"))
      case (400, error, _) => Left(TorErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def syncLdap(henkilöOid: String) = {
    http("/authentication-service/resources/ldap/" + henkilöOid)(Http.expectSuccess)
  }
}

object AuthenticationServiceClient {
  def apply(config: Config) = {
    val virkalijaUrl: Path = if (config.hasPath("authentication-service.virkailija.url")) { config.getString("authentication-service.virkailija.url") } else { config.getString("opintopolku.virkailija.url") }
    val username =  if (config.hasPath("authentication-service.username")) { config.getString("authentication-service.username") } else { config.getString("opintopolku.virkailija.username") }
    val password =  if (config.hasPath("authentication-service.password")) { config.getString("authentication-service.password") } else { config.getString("opintopolku.virkailija.password") }

    val http = VirkailijaHttpClient(username, password, virkalijaUrl, "/authentication-service", config.getBoolean("authentication-service.useCas"))
    new AuthenticationServiceClient(http)
  }
}


case class UserQueryResult(totalCount: Integer, results: List[User])
case class User(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: String, aidinkieli: Option[String], kansalaisuus: Option[List[String]])


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
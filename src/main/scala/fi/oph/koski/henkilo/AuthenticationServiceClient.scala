package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.json.Json4sHttp4s._
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät
import fi.oph.koski.util.Timing
import org.http4s._
import org.http4s.headers.`Content-Type`

import scalaz.concurrent.Task
import scalaz.concurrent.Task.gatherUnordered

trait AuthenticationServiceClient {
  def käyttöoikeusryhmät: List[Käyttöoikeusryhmä]
  def käyttäjänKäyttöoikeusryhmät(oid: String): List[(String, Int)]
  def search(query: String): UserQueryResult
  def create(createUserInfo: CreateUser): Either[HttpStatus, String]
  def findByOid(id: String): Option[User]
  def findByOids(oids: List[String]): List[User]
  def findOrCreate(createUserInfo: CreateUser): Either[HttpStatus, User]
  def organisaationHenkilötRyhmässä(ryhmä: String, organisaatioOid: String) : List[UserWithContactInformation]
}

object AuthenticationServiceClient {
  def apply(config: Config, db: DB) = if (config.hasPath("opintopolku.virkailija.username")) {
    RemoteAuthenticationServiceClient(config)
  } else {
    new MockAuthenticationServiceClientWithDBSupport(db)
  }
}

object RemoteAuthenticationServiceClient {
  def apply(config: Config) = {
    val virkalijaUrl: String = if (config.hasPath("authentication-service.virkailija.url")) { config.getString("authentication-service.virkailija.url") } else { config.getString("opintopolku.virkailija.url") }
    val username =  if (config.hasPath("authentication-service.username")) { config.getString("authentication-service.username") } else { config.getString("opintopolku.virkailija.username") }
    val password =  if (config.hasPath("authentication-service.password")) { config.getString("authentication-service.password") } else { config.getString("opintopolku.virkailija.password") }

    val http = VirkailijaHttpClient(username, password, virkalijaUrl, "/authentication-service", config.getBoolean("authentication-service.useCas"))
    new RemoteAuthenticationServiceClient(http)
  }
}

class RemoteAuthenticationServiceClient(http: Http) extends AuthenticationServiceClient with EntityDecoderInstances with Timing {
  def search(query: String): UserQueryResult = {
    runTask(http(uri"/authentication-service/resources/henkilo?no=true&count=0&q=${query}")(Http.parseJson[UserQueryResult]))
  }

  def findByOid(id: String): Option[User] = findByOids(List(id)).headOption
  def findByOids(oids: List[String]): List[User] = http.post(uri"/authentication-service/resources/s2s/koski/henkilotByHenkiloOidList", oids)(json4sEncoderOf[List[String]], Http.parseJson[List[User]])

  def käyttöoikeusryhmät: List[Käyttöoikeusryhmä] = runTask(http(uri"/authentication-service/resources/kayttooikeusryhma")(Http.parseJson[List[Käyttöoikeusryhmä]]))

  def käyttäjänKäyttöoikeusryhmät(oid: String): List[(String, Int)] = {
    runTask(http(uri"/authentication-service/resources/s2s/koski/kayttooikeusryhmat/${oid}")(Http.parseJson[Map[String, List[Int]]]).map { groupedByOrg =>
      groupedByOrg.toList.flatMap { case (org, ryhmät) => ryhmät.map(ryhmä => (org, ryhmä)) }
    })
  }

  def lisääOrganisaatio(henkilöOid: String, organisaatioOid: String, nimike: String) = {
    http.put(uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo", List(
      LisääOrganisaatio(organisaatioOid, nimike)
    ))(json4sEncoderOf[List[LisääOrganisaatio]], Http.unitDecoder)
  }
  def lisääKäyttöoikeusRyhmä(henkilöOid: String, organisaatioOid: String, ryhmä: Int): Unit = {
    http.put(
      uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo/${organisaatioOid}/kayttooikeusryhmat",
      List(LisääKäyttöoikeusryhmä(ryhmä))
    )(json4sEncoderOf[List[LisääKäyttöoikeusryhmä]], Http.unitDecoder)
  }

  def luoKäyttöoikeusryhmä(tiedot: UusiKäyttöoikeusryhmä) = {
    http.post(
      uri"/authentication-service/resources/kayttooikeusryhma",
      tiedot
    )(json4sEncoderOf[UusiKäyttöoikeusryhmä], Http.unitDecoder)
  }

  def muokkaaKäyttöoikeusryhmä(id: Int, tiedot: UusiKäyttöoikeusryhmä) = {
    http.put(
      uri"/authentication-service/resources/kayttooikeusryhma/$id/kayttooikeus",
      tiedot
    )(json4sEncoderOf[UusiKäyttöoikeusryhmä], Http.unitDecoder)
  }

  def asetaSalasana(henkilöOid: String, salasana: String) = {
    http.post (uri"/authentication-service/resources/salasana/${henkilöOid}", salasana)(EntityEncoder.stringEncoder(Charset.`UTF-8`)
      .withContentType(`Content-Type`(MediaType.`application/json`)), Http.unitDecoder) // <- yes, the API expects media type application/json, but consumes inputs as text/plain
  }
  def findOrCreate(createUserInfo: CreateUser): Either[HttpStatus, User] = {
    val request: Request = Request(uri = uri"/authentication-service/resources/s2s/koski/henkilo", method = Method.POST)
    runTask(http(request.withBody(createUserInfo)(json4sEncoderOf[CreateUser])) {
      case (200, data, _) => Right(Json.read[User](data))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def create(createUserInfo: CreateUser): Either[HttpStatus, String] = {
    val request: Request = Request(uri = uri"/authentication-service/resources/henkilo", method = Method.POST)
    runTask(http(request.withBody(createUserInfo)(json4sEncoderOf[CreateUser])) {
      case (200, oid, _) => Right(oid)
      case (400, "socialsecuritynr.already.exists", _) => Left(KoskiErrorCategory.conflict.hetu("Henkilötunnus on jo olemassa"))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def syncLdap(henkilöOid: String) = {
    http(uri"/authentication-service/resources/ldap/${henkilöOid}")(Http.expectSuccess)
  }

  override def organisaationHenkilötRyhmässä(ryhmä: String, organisaatioOid: String): List[UserWithContactInformation] = {
    val henkilötQuery: Task[UserQueryResult] = http(uri"/authentication-service/resources/henkilo?groupName=${ryhmä}&ht=VIRKAILIJA&no=false&org=${organisaatioOid}&p=false")(Http.parseJson[UserQueryResult])
    runTask(henkilötQuery.flatMap{h =>
      gatherUnordered(h.results.map { u =>
        http(uri"/authentication-service/resources/henkilo/${u.oidHenkilo}")(Http.parseJson[UserWithContactInformation])
      })
    })
  }
}

case class UserQueryResult(totalCount: Integer, results: List[UserQueryUser])
case class UserQueryUser(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String])
case class UserWithContactInformation(oidHenkilo: String, yhteystiedotRyhma: List[YhteystietoRyhmä]) {
  def workEmails: List[String] = {
    yhteystiedotRyhma.collect {
      case YhteystietoRyhmä(_, kuvaus, yhteystiedot) if kuvaus == "yhteystietotyyppi2" => yhteystiedot.collect {
        case Yhteystieto(tyyppi, arvo) if tyyppi == "YHTEYSTIETO_SAHKOPOSTI" => arvo
      }
    }.flatten
  }
}

case class User(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String], aidinkieli: Option[String], kansalaisuus: Option[List[String]])

case class CreateUser(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String, kayttajatiedot: Option[Käyttajatiedot])
case class Käyttajatiedot(username: String)

object CreateUser {
  def palvelu(nimi: String) = CreateUser(None, nimi, "_", "_", "PALVELU", Some(Käyttajatiedot(nimi)))
  def oppija(hetu: String, sukunimi: String, etunimet: String, kutsumanimi: String) = CreateUser(Some(hetu), sukunimi, etunimet, kutsumanimi, "OPPIJA", None)
}


case class OrganisaatioHenkilö(organisaatioOid: String, passivoitu: Boolean)
case class LisääKäyttöoikeusryhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)

case class LisääOrganisaatio(organisaatioOid: String, tehtavanimike: String, passivoitu: Boolean = false, newOne: Boolean = true)

case class Käyttöoikeusryhmä(id: Int, name: String) {
  def toKoskiKäyttöoikeusryhmä = {
    val name: String = this.name.replaceAll("_.*", "")
    Käyttöoikeusryhmät.byName(name)
  }
}

case class UusiKäyttöoikeusryhmä(ryhmaNameFi: String, ryhmaNameSv: String, ryhmaNameEn: String,
                                 palvelutRoolit: List[Void] = Nil, organisaatioTyypit: List[String] = Nil, slaveIds: List[Void] = Nil)

case class YhteystietoRyhmä(id: Int, ryhmaKuvaus: String, yhteystiedot: List[Yhteystieto])
case class Yhteystieto(yhteystietoTyyppi: String, yhteystietoArvo: String)
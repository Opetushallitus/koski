package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.henkilo.AuthenticationServiceClient._
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
  def search(query: String): HenkilöQueryResult
  def create(createUserInfo: UusiHenkilö): Either[HttpStatus, String]
  def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö]
  def findOppijaByOid(oid: String): Option[OppijaHenkilö]
  def findOppijatByOids(oids: List[String]): List[OppijaHenkilö]
  def findOrCreate(createUserInfo: UusiHenkilö): Either[HttpStatus, OppijaHenkilö]
  def organisaationHenkilötRyhmässä(ryhmä: String, organisaatioOid: String) : List[HenkilöYhteystiedoilla]
}

object AuthenticationServiceClient {
  def apply(config: Config, db: DB) = if (config.hasPath("opintopolku.virkailija.username")) {
    RemoteAuthenticationServiceClient(config)
  } else {
    new MockAuthenticationServiceClientWithDBSupport(db)
  }

  case class HenkilöQueryResult(totalCount: Integer, results: List[QueryHenkilö])

  case class QueryHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String])
  case class OppijaHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String], aidinkieli: Option[String], kansalaisuus: Option[List[String]]) {
    def toQueryHenkilö = QueryHenkilö(oidHenkilo, sukunimi, etunimet, kutsumanimi, hetu)
  }

  case class HenkilöYhteystiedoilla(oidHenkilo: String, yhteystiedotRyhma: List[YhteystietoRyhmä]) {
    def workEmails: List[String] = {
      yhteystiedotRyhma.collect {
        case YhteystietoRyhmä(_, kuvaus, yhteystiedot) if kuvaus == "yhteystietotyyppi2" => yhteystiedot.collect {
          case Yhteystieto(tyyppi, arvo) if tyyppi == "YHTEYSTIETO_SAHKOPOSTI" => arvo
        }
      }.flatten
    }
  }

  case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, kayttajatiedot: Option[Käyttäjätiedot])
  case class UusiHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String, kayttajatiedot: Option[Käyttäjätiedot])
  case class Käyttäjätiedot(username: Option[String])

  object UusiHenkilö {
    def palvelu(nimi: String) = UusiHenkilö(None, nimi, "_", "_", "PALVELU", Some(Käyttäjätiedot(Some(nimi))))
    def oppija(hetu: String, sukunimi: String, etunimet: String, kutsumanimi: String) = UusiHenkilö(Some(hetu), sukunimi, etunimet, kutsumanimi, "OPPIJA", None)
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

  case class UusiKäyttöoikeusryhmä(ryhmaNameFi: String, ryhmaNameSv: String, ryhmaNameEn: String, palvelutRoolit: List[Palvelurooli] = Nil, organisaatioTyypit: List[String] = Nil, slaveIds: List[Void] = Nil)

  case class Palvelurooli(palveluName: String, rooli: String)
  object Palvelurooli {
    def apply(rooli: String): Palvelurooli = Palvelurooli("KOSKI", rooli)
  }
  case class YhteystietoRyhmä(id: Int, ryhmaKuvaus: String, yhteystiedot: List[Yhteystieto])
  case class Yhteystieto(yhteystietoTyyppi: String, yhteystietoArvo: String)
}

object RemoteAuthenticationServiceClient {
  def apply(config: Config) = {
    val virkalijaUrl: String = if (config.hasPath("authentication-service.virkailija.url")) { config.getString("authentication-service.virkailija.url") } else { config.getString("opintopolku.virkailija.url") }
    val username =  if (config.hasPath("authentication-service.username")) { config.getString("authentication-service.username") } else { config.getString("opintopolku.virkailija.username") }
    val password =  if (config.hasPath("authentication-service.password")) { config.getString("authentication-service.password") } else { config.getString("opintopolku.virkailija.password") }
    val authServiceHttp = VirkailijaHttpClient(username, password, virkalijaUrl, "/authentication-service", config.getBoolean("authentication-service.useCas"))
    val oidServiceHttp = VirkailijaHttpClient(username, password, virkalijaUrl, "/oppijanumerorekisteri-service", config.getBoolean("authentication-service.useCas"))
    val käyttöOikeusHttp = VirkailijaHttpClient(username, password, virkalijaUrl, "/kayttooikeus-service", config.getBoolean("authentication-service.useCas"))
    (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) match {
      case false => new RemoteAuthenticationServiceClient(authServiceHttp, oidServiceHttp, käyttöOikeusHttp)
      case true => new RemoteAuthenticationServiceClientWithMockOids(authServiceHttp, oidServiceHttp, käyttöOikeusHttp)
    }
  }
}

class RemoteAuthenticationServiceClient(authServiceHttp: Http, oidServiceHttp: Http, käyttöOikeusHttp: Http) extends AuthenticationServiceClient with EntityDecoderInstances with Timing {
  def search(query: String): HenkilöQueryResult = {
    runTask(authServiceHttp.get(uri"/authentication-service/resources/henkilo?no=true&count=0&q=${query}")(Http.parseJson[HenkilöQueryResult]))
  }

  def findOppijaByOid(oid: String): Option[OppijaHenkilö] = findOppijatByOids(List(oid)).headOption
  def findOppijatByOids(oids: List[String]): List[OppijaHenkilö] = runTask(oidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[List[OppijaHenkilö]]))

  def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = runTask(authServiceHttp.get(uri"/authentication-service/resources/henkilo/${oid}")(Http.parseJsonIgnoreError[KäyttäjäHenkilö])) // ignore error, because the API returns status 500 instead of 404 when not found

  def käyttöoikeusryhmät: List[Käyttöoikeusryhmä] = runTask(käyttöOikeusHttp.get(uri"/kayttooikeus-service/kayttooikeusryhma")(Http.parseJson[List[Käyttöoikeusryhmä]]))

  def lisääOrganisaatio(henkilöOid: String, organisaatioOid: String, nimike: String) = {
    authServiceHttp.put(uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo", List(
      LisääOrganisaatio(organisaatioOid, nimike)
    ))(json4sEncoderOf[List[LisääOrganisaatio]])(Http.unitDecoder)
  }
  def lisääKäyttöoikeusRyhmä(henkilöOid: String, organisaatioOid: String, ryhmä: Int): Unit = {
    authServiceHttp.put(
      uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo/${organisaatioOid}/kayttooikeusryhmat",
      List(LisääKäyttöoikeusryhmä(ryhmä))
    )(json4sEncoderOf[List[LisääKäyttöoikeusryhmä]])(Http.unitDecoder)
  }

  def asetaSalasana(henkilöOid: String, salasana: String) = {
    authServiceHttp.post (uri"/authentication-service/resources/salasana/${henkilöOid}", salasana)(EntityEncoder.stringEncoder(Charset.`UTF-8`)
      .withContentType(`Content-Type`(MediaType.`application/json`)))(Http.unitDecoder) // <- yes, the API expects media type application/json, but consumes inputs as text/plain
  }
  def findOrCreate(createUserInfo: UusiHenkilö): Either[HttpStatus, OppijaHenkilö] = {
    runTask(authServiceHttp.post(uri"/authentication-service/resources/s2s/koski/henkilo", createUserInfo)(json4sEncoderOf[UusiHenkilö]) {
      case (200, data, _) => Right(Json.read[OppijaHenkilö](data))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def create(createUserInfo: UusiHenkilö): Either[HttpStatus, String] = {
    runTask(authServiceHttp.post(uri"/authentication-service/resources/henkilo", createUserInfo)(json4sEncoderOf[UusiHenkilö]) {
      case (200, oid, _) => Right(oid)
      case (400, "socialsecuritynr.already.exists", _) => Left(KoskiErrorCategory.conflict.hetu("Henkilötunnus on jo olemassa"))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw new HttpStatusException(status, text, uri)
    })
  }
  def syncLdap(henkilöOid: String) = {
    authServiceHttp.get(uri"/authentication-service/resources/ldap/${henkilöOid}")(Http.expectSuccess)
  }

  override def organisaationHenkilötRyhmässä(ryhmä: String, organisaatioOid: String): List[HenkilöYhteystiedoilla] = {
    val henkilötQuery: Task[HenkilöQueryResult] = authServiceHttp.get(uri"/authentication-service/resources/henkilo?groupName=${ryhmä}&ht=VIRKAILIJA&no=false&org=${organisaatioOid}&p=false")(Http.parseJson[HenkilöQueryResult])
    runTask(henkilötQuery.flatMap{h =>
      gatherUnordered(h.results.map { u =>
        authServiceHttp.get(uri"/authentication-service/resources/henkilo/${u.oidHenkilo}")(Http.parseJson[HenkilöYhteystiedoilla])
      })
    })
  }
}

class RemoteAuthenticationServiceClientWithMockOids(authServiceHttp: Http, oidServiceHttp: Http, käyttöOikeusHttp: Http) extends RemoteAuthenticationServiceClient(authServiceHttp, oidServiceHttp, käyttöOikeusHttp) {
  override def findOppijatByOids(oids: List[String]): List[OppijaHenkilö] = {
    val found = super.findOppijatByOids(oids).map(henkilö => (henkilö.oidHenkilo, henkilö)).toMap
    oids.map { oid =>
      found.get(oid) match {
        case Some(henkilö) => henkilö
        case None => OppijaHenkilö(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Testihenkilö", "Testihenkilö", Some("010101-123N"), None, None)
      }
    }
  }

  override def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = super.findKäyttäjäByOid(oid).orElse {
    Some(KäyttäjäHenkilö(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Tuntematon", "Tuntematon", None))
  }
}
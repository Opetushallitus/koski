package fi.oph.koski.henkilo.authenticationservice

import com.typesafe.config.Config
import fi.oph.koski.henkilo.RemoteOpintopolkuHenkilöFacade
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.UusiHenkilö
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import org.http4s.headers.`Content-Type`
import org.http4s.{Charset, EntityEncoder, MediaType}

import scalaz.concurrent.Task

case class AuthenticationServiceClient(config: Config) {
  val authServiceHttp = VirkailijaHttpClient(RemoteOpintopolkuHenkilöFacade.makeServiceConfig(config), "/authentication-service", config.getBoolean("authentication-service.useCas"))

  def findByKäyttöoikeusryhmä(ryhmä: String, organisaatioOid: String) = authServiceHttp.get(uri"/authentication-service/resources/henkilo?groupName=$ryhmä&ht=VIRKAILIJA&no=false&org=$organisaatioOid&p=false")(Http.parseJson[HenkilöQueryResult])


  def asetaSalasana(henkilöOid: String, salasana: String): Task[Unit] =
    authServiceHttp.post (uri"/authentication-service/resources/salasana/${henkilöOid}", salasana)(EntityEncoder.stringEncoder(Charset.`UTF-8`)
      .withContentType(`Content-Type`(MediaType.`application/json`)))(Http.unitDecoder) // <- yes, the API expects media type application/json, but consumes inputs as text/plain

  def syncLdap(henkilöOid: String): Task[Unit] =
    authServiceHttp.get(uri"/authentication-service/resources/ldap/${henkilöOid}")(Http.expectSuccess)

  def lisääOrganisaatio(henkilöOid: String, organisaatioOid: String, nimike: String): Task[Unit] =
    authServiceHttp.put(uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo", List(
      LisääOrganisaatio(organisaatioOid, nimike)
    ))(json4sEncoderOf[List[LisääOrganisaatio]])(Http.unitDecoder)

  def lisääKäyttöoikeusRyhmä(henkilöOid: String, organisaatioOid: String, ryhmä: Int): Unit =
    authServiceHttp.put(
      uri"/authentication-service/resources/henkilo/${henkilöOid}/organisaatiohenkilo/${organisaatioOid}/kayttooikeusryhmat",
      List(LisääKäyttöoikeusryhmä(ryhmä))
    )(json4sEncoderOf[List[LisääKäyttöoikeusryhmä]])(Http.unitDecoder)

  def create(createUserInfo: UusiHenkilö): Either[HttpStatus, String] =
    runTask(authServiceHttp.post(uri"/authentication-service/resources/henkilo", createUserInfo)(json4sEncoderOf[UusiHenkilö]) {
      case (200, oid, _) => Right(oid)
      case (400, "socialsecuritynr.already.exists", _) => Left(KoskiErrorCategory.conflict.hetu("Henkilötunnus on jo olemassa"))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    })

  def search(query: String): HenkilöQueryResult =
    runTask(authServiceHttp.get(uri"/authentication-service/resources/henkilo?no=true&count=0&q=${query}")(parseJson[HenkilöQueryResult]))
}

case class LisääKäyttöoikeusryhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)
case class LisääOrganisaatio(organisaatioOid: String, tehtavanimike: String, passivoitu: Boolean = false, newOne: Boolean = true)
case class QueryHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String])
case class HenkilöQueryResult(totalCount: Int, results: List[QueryHenkilö])
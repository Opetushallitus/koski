package fi.oph.koski.tools

import com.typesafe.config.Config
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.{HenkilöQueryResult, UusiHenkilö}
import fi.oph.koski.http._
import fi.oph.koski.json.Json._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät
import fi.oph.koski.log.Logging
import org.http4s.headers.`Content-Type`
import org.http4s.{Charset, EntityDecoderInstances, EntityEncoder, MediaType}

import scalaz.concurrent.Task

object ServiceUserAdder extends App with Logging {
  val app: KoskiApplication = KoskiApplicationForTests
  val authService = new AuthServiceClient(app.config)
  val kp = app.koodistoPalvelu

  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get
      val oid: String = luoKäyttäjä(username)

      asetaOrganisaatioJaRyhmät(organisaatioOid, oid)

      authService.asetaSalasana(oid, password)
      authService.syncLdap(oid)
      logger.info("Set password " + password + ", requested LDAP sync")

      validoiLähdejärjestelmä(lahdejarjestelma)

      println(
        s"""Hei,
          |
          |Pyysitte testitunnuksia Koski-järjestelmään. Loimme käyttöönne tunnuksen:
          |
          |käyttäjätunnus: ${username}
          |salasana: ${password}
          |
          |Tunnuksella on oikeus luoda/katsella/päivittää Kosken opiskeluoikeuksia organisaatiossa ${organisaatioOid} (${organisaatio.nimi.get.get("fi")}).
          |
          |Tunnus on palvelukäyttäjätyyppinen, joten kaikkiin PUT-rajapinnan kautta lähetettyihin opiskeluoikeuksiin täytyy liittää lähdejärjestelmänId-kenttä seuraavasti:
          |
          |    "lähdejärjestelmänId": { "id": "xxxx12345", "lähdejärjestelmä": { "koodiarvo": "${lahdejarjestelma}", "koodistoUri": "lahdejarjestelma" } }
          |
          |Korvatkaa esimerkkinä käytetty id "xxxx12345" tunnisteella, jota lähdejärjestelmässänne käytetään identifioimaan kyseinen opiskeluoikeus.
          |
          |Koski-testijärjestelmän etusivu: https://extra.koski.opintopolku.fi/koski/
          |API-dokumentaatiosivu: https://extra.koski.opintopolku.fi/koski/documentation
          |
          |Tervetuloa keskustelemaan Koski-järjestelmän kehityksestä ja käytöstä yhteistyökanavallemme Flowdockiin! Lähetän sinne erilliset kutsut kohta.
          |
          |Ystävällisin terveisin,
          |___________________""".stripMargin)
    case Array(userOid, organisaatioOid) =>
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get
      asetaOrganisaatioJaRyhmät(organisaatioOid, userOid)
    case _ =>
      logger.info(
        """Usage: ServiceUserAdder <username> <organisaatio-oid> <salasana> <lahdejärjestelmä-id>
          |       ServiceUserAdder <user-oid> <organisaatio-oid>
        """.stripMargin)
  }

  def validoiLähdejärjestelmä(lahdejarjestelma: String): Unit = {
    val koodiarvo = lahdejarjestelma
    val koodisto = kp.getLatestVersion("lahdejarjestelma").get

    if (!kp.getKoodistoKoodit(koodisto).toList.flatten.find(_.koodiArvo == koodiarvo).isDefined) {
      throw new RuntimeException("Tuntematon lähdejärjestelmä " + koodiarvo)
    }
  }

  def asetaOrganisaatioJaRyhmät(organisaatioOid: String, oid: String): Unit = {
    authService.lisääOrganisaatio(oid, organisaatioOid, "oppilashallintojärjestelmä")

    val ryhmät = List(Käyttöoikeusryhmät.oppilaitosPalvelukäyttäjä)

    ryhmät.foreach { ryhmä =>
      val käyttöoikeusryhmäId = authService.käyttöoikeusryhmät.find(_.toKoskiKäyttöoikeusryhmä.map(_.nimi) == Some(ryhmä.nimi)).get.id
      authService.lisääKäyttöoikeusRyhmä(oid, organisaatioOid, käyttöoikeusryhmäId)
    }
  }

  def luoKäyttäjä(username: String): String = {
    val oid = authService.create(UusiHenkilö.palvelu(username)) match {
      case Right(oid) =>
        logger.info("User created")
        oid
      case Left(HttpStatus(400, _)) => authService.search(username) match {
        case r: HenkilöQueryResult if (r.totalCount == 1) => r.results(0).oidHenkilo
      }
    }
    logger.info("Username " + username + ", oid: " + oid)
    oid
  }
}

class AuthServiceClient(config: Config) extends EntityDecoderInstances {
  import Http._
  private val virkalijaUrl = if (config.hasPath("authentication-service.virkailija.url")) { config.getString("authentication-service.virkailija.url") } else { config.getString("opintopolku.virkailija.url") }
  private val username =  if (config.hasPath("authentication-service.username")) { config.getString("authentication-service.username") } else { config.getString("opintopolku.virkailija.username") }
  private val password =  if (config.hasPath("authentication-service.password")) { config.getString("authentication-service.password") } else { config.getString("opintopolku.virkailija.password") }
  private val authServiceHttp = VirkailijaHttpClient(username, password, virkalijaUrl, "/authentication-service", config.getBoolean("authentication-service.useCas"))
  private val käyttöOikeusHttp = VirkailijaHttpClient(username, password, virkalijaUrl, "/kayttooikeus-service", config.getBoolean("authentication-service.useCas"))

  def käyttöoikeusryhmät: List[Käyttöoikeusryhmä] =
    runTask(käyttöOikeusHttp.get(uri"/kayttooikeus-service/kayttooikeusryhma")(parseJson[List[Käyttöoikeusryhmä]]))

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

case class Käyttöoikeusryhmä(id: Int, name: String) {
  def toKoskiKäyttöoikeusryhmä = {
    val name: String = this.name.replaceAll("_.*", "")
    Käyttöoikeusryhmät.byName(name)
  }
}

case class LisääOrganisaatio(organisaatioOid: String, tehtavanimike: String, passivoitu: Boolean = false, newOne: Boolean = true)
case class LisääKäyttöoikeusryhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)


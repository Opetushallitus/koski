package fi.oph.koski.henkilo

import java.time.LocalDate
import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.Koodistokoodiviite
import cats.syntax.parallel._
import fi.oph.koski.log.Logging
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.RetryPolicy

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class OppijanumeroRekisteriClient(
  config: Config,
  retryStrategy: OppijanumeroRekisteriClientRetryStrategy = OppijanumeroRekisteriClientRetryStrategy.Default,
) {
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): IO[Either[HttpStatus, SuppeatOppijaHenkilöTiedot]] =
    oidServiceHttp.post(uri"/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto", createUserInfo)(json4sEncoderOf[UusiOppijaHenkilö]) {
      case (x, data, _) if x <= 201 => Right(JsonSerializer.parse[OppijaNumerorekisteriPerustiedot](data, ignoreExtras = true))
      case (400, error, _) => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.virheelliset(error))
      case (status, text, uri) => throw HttpStatusException(status, text, uri)
    }.flatMap {
      case Right(o) => findSlaveOids(o.oidHenkilo).map(o.toSuppeaOppijaHenkilö).map(Right(_))
      case Left(status) => IO.pure(status).map(Left(_))
    }

  def withRetryStrategy(strategy: OppijanumeroRekisteriClientRetryStrategy): OppijanumeroRekisteriClient =
    this.copy(retryStrategy = strategy)

  private val onrBaseUrl = config.getString("oppijanumerorekisteri.baseUrl")

  private val baseUrl = "/oppijanumerorekisteri-service"

  private val otuvaTokenEndpoint = config.getString("otuvaTokenEndpoint")

  private val oauth2clientFactory: (String, Client[IO]) => Http = {
    if (otuvaTokenEndpoint == "mock") {
      (root: String, client: Client[IO]) => {
        Http(root, ClientWithBasicAuthentication(client,
          config.getString("opintopolku.virkailija.username"),
          config.getString("opintopolku.virkailija.password"),
        ))
      }
    } else {
      new OtuvaOAuth2ClientFactory(OtuvaOAuth2Credentials.fromSecretsManager, otuvaTokenEndpoint).apply
    }
  }

  private val oidServiceHttp = oauth2clientFactory(onrBaseUrl, Http.retryingClient(baseUrl))

  private val postRetryingOidServiceHttp = {
    // Osa POST-metodilla ONR:ään tehtävistä kyselyistä on oikeasti idempotentteja,
    // joten niiden uudelleenyrittäminen on ok: siksi unsafeRetryingClient. Tällä saadaan
    // esim. raportointoinkannan generointi jatkamaan, vaikka onr-yhteys hetken pätkisikin.
    val client = unsafeRetryingClient(baseUrl, retryStrategy.applyConfig, retryStrategy.backoffPolicy)

    oauth2clientFactory(onrBaseUrl, client)
  }

  private def henkilöByOid[T](oid: String) =
    oidServiceHttp.get[T](uri"/oppijanumerorekisteri-service/henkilo/$oid")(_)

  def findKäyttäjäByOid(oid: String): IO[Option[KäyttäjäHenkilö]] = henkilöByOid(oid)(Http.parseJsonOptional[KäyttäjäHenkilö])

  def findOppijatNoSlaveOids(oids: Seq[Oid]): IO[List[SuppeatOppijaHenkilöTiedot]] =
    findOnrOppijatByOids(oids).map(_.map(_.toSuppeaOppijaHenkilö(Nil)))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): IO[List[Oid]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/changedSince/$since?offset=$offset&amount=$amount")(Http.parseJson[List[String]])

  def findByVarhaisinSyntymäaikaAndKotikunta(syntymäaika: String, kunta: String, page: Int): IO[OppijaNumerorekisteriKuntarouhintatiedot] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/s2s/henkilo/list/$kunta/$syntymäaika?page=$page")(Http.parseJson[OppijaNumerorekisteriKuntarouhintatiedot])

  def findOppijaByOid(oid: Oid): IO[Option[LaajatOppijaHenkilöTiedot]] =
    henkilöByOid(oid)(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .map(_.toSeq).flatMap(withSlaveOids(_).map(_.headOption))

  def findOppijaByHetu(hetu: String): IO[Option[LaajatOppijaHenkilöTiedot]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/hetu=$hetu")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(withSlaveOids(_).map(_.headOption))

  def findMasterOppija(oid: String): IO[Option[LaajatOppijaHenkilöTiedot]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$oid/master")(Http.parseJsonOptional[OppijaNumerorekisteriOppija])
      .flatMap(withSlaveOids(_).map(_.headOption))

  def findMasterOppijat(oids: List[String]): IO[Map[String, LaajatOppijaHenkilöTiedot]] =
    postRetryingOidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/masterHenkilosByOidList", oids)(json4sEncoderOf[List[String]])(Http.parseJson[Map[String, OppijaNumerorekisteriOppija]])
    .map(_.mapValues(_.toOppijaHenkilö(Nil)))

  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): IO[List[SuppeatOppijaHenkilöTiedot]] =
    postRetryingOidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList", hetus)(json4sEncoderOf[Seq[String]])(Http.parseJson[List[OppijaNumerorekisteriPerustiedot]])
      .map(_.map(_.toSuppeaOppijaHenkilö(Nil)))

  def findSlaveOids(masterOid: String): IO[List[String]] =
    oidServiceHttp.get(uri"/oppijanumerorekisteri-service/henkilo/$masterOid/slaves")(Http.parseJson[List[OppijaNumerorekisteriSlave]]).map(_.map(_.oidHenkilo))

  def findKotikuntahistoria(masterOids: Seq[String], turvakiellolliset: Boolean): IO[List[OppijanumerorekisteriKotikuntahistoriaRow]] = {
    val url = if (turvakiellolliset) uri"/oppijanumerorekisteri-service/s2s/henkilo/kotikuntahistoria/turvakielto" else uri"/oppijanumerorekisteri-service/s2s/henkilo/kotikuntahistoria"
    oidServiceHttp.post(url, masterOids)(json4sEncoderOf[Seq[String]])(Http.parseJson[List[OppijanumerorekisteriKotikuntahistoriaRow]])
  }

  private def findOnrOppijatByOids(oids: Seq[Oid]): IO[List[OppijaNumerorekisteriPerustiedot]] =
    postRetryingOidServiceHttp.post(uri"/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList", oids)(json4sEncoderOf[Seq[String]])(Http.parseJson[List[OppijaNumerorekisteriPerustiedot]])

  private def withSlaveOids(onrOppijat: Iterable[OppijaNumerorekisteriOppija]): IO[List[LaajatOppijaHenkilöTiedot]] =
    onrOppijat.toList.parTraverse(complementWithSlaveOids)

  private def complementWithSlaveOids(onrOppija: OppijaNumerorekisteriOppija): IO[LaajatOppijaHenkilöTiedot] =
    findSlaveOids(onrOppija.oidHenkilo).map(onrOppija.toOppijaHenkilö)
}

case class OppijanumeroRekisteriClientRetryStrategy(
  maxRetries: Int,
  maxWaitBetweenRetries: FiniteDuration,
  retryTimeout: FiniteDuration,
) extends Logging {
  //  responseHeaderTimeout < requestTimeout < idleTimeout
  val maxTotalWaitTimeBetweenRetries: FiniteDuration = maxWaitBetweenRetries * (maxRetries - 1)

  val requestTimeout: FiniteDuration = maxTotalWaitTimeBetweenRetries + retryTimeout * maxRetries
  val idleTimeout: FiniteDuration = requestTimeout + 2.seconds

  logger.info(s"Oppijanumerorekisteri retry strategy created: max retries = $maxRetries, max wait between retries = $maxWaitBetweenRetries, retry timeout = $retryTimeout, request timeout = $requestTimeout, idle timeout = $idleTimeout, total timeout = $requestTimeout")

  def applyConfig(builder: BlazeClientBuilder[IO]): BlazeClientBuilder[IO] = {
    builder
      .withConnectTimeout(retryTimeout - 1.seconds)
      .withResponseHeaderTimeout(retryTimeout)
      .withRequestTimeout(requestTimeout)
      .withIdleTimeout(idleTimeout)
  }

  def backoffPolicy: Int => Option[FiniteDuration] = RetryPolicy.exponentialBackoff(
    maxWait = maxWaitBetweenRetries,
    maxRetry = maxRetries
  )
}

object OppijanumeroRekisteriClientRetryStrategy {
  val Default: OppijanumeroRekisteriClientRetryStrategy =
    OppijanumeroRekisteriClientRetryStrategy(
      maxRetries = 5,
      maxWaitBetweenRetries = 2.seconds,
      retryTimeout = 10.seconds,
    )
}

case class KäyttäjäHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, asiointiKieli: Option[Kieli])
case class OppijaNumerorekisteriSlave(oidHenkilo: String)

case class OppijaNumerorekisteriPerustiedot(
  oidHenkilo: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: Option[String],
  hetu: Option[String],
  syntymaaika: Option[LocalDate],
  aidinkieli: Option[Kieli],
  kansalaisuus: Option[List[Kansalaisuus]],
  modified: Long,
  turvakielto: Option[Boolean],
  sukupuoli: Option[String],
  kotikunta: Option[String],
  yksiloity: Option[Boolean],
  yksiloityVTJ: Option[Boolean]
) {
  def toSuppeaOppijaHenkilö(linkitetytOidit: List[String]) = SuppeatOppijaHenkilöTiedot(
    oid = oidHenkilo,
    sukunimi = sukunimi,
    etunimet = etunimet,
    kutsumanimi = kutsumanimi.getOrElse(etunimet.trim.split(" ").head),
    hetu = hetu,
    syntymäaika = syntymaaika,
    äidinkieli = aidinkieli.map(_.kieliKoodi),
    kansalaisuus = kansalaisuus.map(_.map(_.kansalaisuusKoodi)),
    modified = modified,
    turvakielto = turvakielto.getOrElse(false),
    sukupuoli = sukupuoli,
    linkitetytOidit = linkitetytOidit
  )
}

case class OppijaNumerorekisteriOppija(
  oidHenkilo: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: Option[String],
  hetu: Option[String],
  syntymaaika: Option[LocalDate],
  aidinkieli: Option[Kieli],
  kansalaisuus: Option[List[Kansalaisuus]],
  kaikkiHetut: Option[List[String]],
  modified: Long,
  turvakielto: Option[Boolean],
  sukupuoli: Option[String],
  kotikunta: Option[String],
  yksiloity: Option[Boolean],
  yksiloityVTJ: Option[Boolean],
  yhteystiedotRyhma: Seq[OppijaNumerorekisteriYhteystiedotRyhma]
) {
  def toOppijaHenkilö(linkitetytOidit: List[String]) = LaajatOppijaHenkilöTiedot(
    oid = oidHenkilo,
    sukunimi = sukunimi,
    etunimet = etunimet,
    kutsumanimi = kutsumanimi.getOrElse(etunimet.trim.split(" ").head),
    hetu = hetu,
    syntymäaika = syntymaaika,
    äidinkieli = aidinkieli.map(_.kieliKoodi),
    kansalaisuus = kansalaisuus.map(_.map(_.kansalaisuusKoodi)),
    modified = modified,
    turvakielto = turvakielto.getOrElse(false),
    sukupuoli = sukupuoli,
    linkitetytOidit = linkitetytOidit,
    vanhatHetut = kaikkiHetut.getOrElse(Nil).filterNot(hetu.contains),
    kotikunta = kotikunta,
    yksilöity = yksiloity.exists(identity) || yksiloityVTJ.exists(identity),
    yhteystiedot = yhteystiedotRyhma.map(_.toYhteystiedot)
  )
}
case class UusiOppijaHenkilö(hetu: Option[String], sukunimi: String, etunimet: String, kutsumanimi: String, henkiloTyyppi: String = "OPPIJA")

case class Kansalaisuus(kansalaisuusKoodi: String)
case class Kieli(kieliKoodi: String)

case class OppijaNumerorekisteriYhteystiedotRyhma(
  id: Int,
  readOnly: Boolean,
  ryhmaAlkuperaTieto: String,
  ryhmaKuvaus: String,
  yhteystieto: Seq[OppijaNumerorekisteriYhteystieto]
) {
  def toYhteystiedot: Yhteystiedot = Yhteystiedot(
    alkuperä = Koodistokoodiviite(ryhmaAlkuperaTieto, "yhteystietojenalkupera"),
    tyyppi = Koodistokoodiviite(ryhmaKuvaus, "yhteystietotyypit"),
    sähköposti = yhteystietoarvo("YHTEYSTIETO_SAHKOPOSTI"),
    puhelinnumero = yhteystietoarvo("YHTEYSTIETO_PUHELINNUMERO"),
    matkapuhelinnumero = yhteystietoarvo("YHTEYSTIETO_MATKAPUHELINNUMERO"),
    katuosoite = yhteystietoarvo("YHTEYSTIETO_KATUOSOITE"),
    kunta = yhteystietoarvo("YHTEYSTIETO_KUNTA"),
    postinumero = yhteystietoarvo("YHTEYSTIETO_POSTINUMERO"),
    kaupunki = yhteystietoarvo("YHTEYSTIETO_KAUPUNKI"),
    maa = yhteystietoarvo("YHTEYSTIETO_MAA"),
  )

  def yhteystietoarvo(tyyppi: String): Option[String] = yhteystieto
    .find(_.yhteystietoTyyppi == tyyppi)
    .flatMap(_.yhteystietoArvo)
}

case class OppijaNumerorekisteriYhteystieto(
  yhteystietoArvo: Option[String],
  yhteystietoTyyppi: String
)

case class OppijaNumerorekisteriKuntarouhintatiedot(
  first: Boolean,
  last: Boolean,
  number: Int,
  numberOfElements: Int,
  size: Int,
  results: Seq[OppijaNumerorekisteriKuntarouhintaOppija]
)

case class OppijaNumerorekisteriKuntarouhintaOppija(
  oidHenkilo: String,
  hetu: Option[String],
  syntymaaika: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
)

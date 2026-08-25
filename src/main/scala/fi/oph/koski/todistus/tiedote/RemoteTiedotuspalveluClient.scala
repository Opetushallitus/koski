package fi.oph.koski.todistus.tiedote

import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http._
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.RetryPolicy

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class KielitutkintoTodistusTiedoteRequest(
  oppijanumero: String,
  opiskeluoikeusOid: String,
  idempotencyKey: String,
  todistusBucket: Option[String],
  todistusKey: Option[String],
  kituExamineeDetails: Option[KituExamineeDetails]
)

/**
 * Tiedote-POSTin uudelleenyritysbudjetti. Yhden yrityksen katkaisee responseHeaderTimeout ja koko
 * loopin httpTimeout, joka on siksi johdettava yritysten määrästä: Http.runRequest kääräisee sen
 * retry-loopin ympärille, joten liian lyhyt arvo katkaisisi uudelleenyritykset hiljaisesti kesken.
 */
object TiedotuspalveluRetryBudget {
  val maxRetries: Int = 3
  val maxWaitBetweenRetries: FiniteDuration = 2.seconds

  /** Yhden yrityksen katkaisuaika. Tiedote-POST on käytännössä yksi DB-insert, joten tämä on reilusti yli tarpeen. */
  val retryTimeout: FiniteDuration = 10.seconds

  /** Uloin timeout koko retry-loopille, ks. Http.runRequest. */
  val httpTimeout: FiniteDuration =
    retryTimeout * (maxRetries + 1) + maxWaitBetweenRetries * maxRetries + 4.seconds

  // requestTimeout ja idleTimeout jätetään oletuksiinsa (60 s / 120 s): ne eivät voi laueta ennen
  // lyhyempää responseHeaderTimeoutia tai uloimpaa httpTimeoutia.
  def applyConfig(builder: BlazeClientBuilder[IO]): BlazeClientBuilder[IO] =
    builder
      .withConnectTimeout(retryTimeout - 1.second)
      .withResponseHeaderTimeout(retryTimeout)

  def backoffPolicy: Int => Option[FiniteDuration] = RetryPolicy.exponentialBackoff(
    maxWait = maxWaitBetweenRetries,
    maxRetry = maxRetries
  )
}

class RemoteTiedotuspalveluClient(config: Config) extends TiedotuspalveluClient with Logging {
  private val baseUrl = config.getString("tiedote.baseUrl")
  private val otuvaTokenEndpoint = config.getString("otuvaTokenEndpoint")

  // unsafeRetryingClient, koska http4s ei oletuksena uudelleenyritä ei-idempotentteja pyyntöjä.
  // Tiedote-POSTin saa turvallisesti yrittää uudelleen: tiedotuspalvelu palauttaa olemassa olevan
  // tiedotteen samalla idempotencyKeyllä (uniikki indeksi idempotency_key-sarakkeella), ja POST on
  // pelkkä jonotus – lähetys oppijalle on tiedotuspalvelun oma ajastettu tehtävä.
  private val http: Http = {
    val client = Http.unsafeRetryingClient(
      "tiedotuspalvelu",
      TiedotuspalveluRetryBudget.applyConfig,
      TiedotuspalveluRetryBudget.backoffPolicy
    )
    val withAuth =
      if (otuvaTokenEndpoint == "mock" && !Environment.isServerEnvironment(config)) {
        Http(baseUrl, client)
      } else {
        new OtuvaOAuth2ClientFactory(OtuvaOAuth2Credentials.fromSecretsManager, otuvaTokenEndpoint)
          .apply(baseUrl, client)
      }
    withAuth.copy(defaultTimeout = TiedotuspalveluRetryBudget.httpTimeout)
  }

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    opiskeluoikeusOid: String,
    idempotencyKey: String,
    todistusBucket: Option[String],
    todistusKey: Option[String],
    kituExamineeDetails: Option[KituExamineeDetails]
  ): Either[HttpStatus, Unit] = {
    val request = KielitutkintoTodistusTiedoteRequest(
      oppijanumero = oppijanumero,
      opiskeluoikeusOid = opiskeluoikeusOid,
      idempotencyKey = idempotencyKey,
      todistusBucket = todistusBucket,
      todistusKey = todistusKey,
      kituExamineeDetails = kituExamineeDetails
    )

    try {
      runIO(
        http.post(uri"/api/v1/tiedote/kielitutkintotodistus", request)(json4sEncoderOf[KielitutkintoTodistusTiedoteRequest])(Http.expectSuccess)
      )

      Right(())
    } catch {
      case e: HttpStatusException =>
        logger.error(s"Tiedotuspalvelu-kutsu epäonnistui: ${e.status} ${e.msg}")
        Left(KoskiErrorCategory.unavailable(s"Tiedotuspalvelu-kutsu epäonnistui: ${e.status}"))
      case e: Exception =>
        logger.error(e)(s"Tiedotuspalvelu-kutsu epäonnistui: ${e.getMessage}")
        Left(KoskiErrorCategory.internalError(s"Tiedotuspalvelu-kutsu epäonnistui: ${e.getMessage}"))
    }
  }
}

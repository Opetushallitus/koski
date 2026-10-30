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

object TiedotuspalveluRetryBudget {
  val maxRetries: Int = 3
  val maxWaitBetweenRetries: FiniteDuration = 2.seconds

  val retryTimeout: FiniteDuration = 30.seconds

  // Http.runRequest kääräisee timeoutin koko retry-loopin ympärille, joten sen on katettava kaikki
  // yritykset: liian lyhyt arvo katkaisisi uudelleenyritykset hiljaisesti kesken.
  val httpTimeout: FiniteDuration =
    retryTimeout * (maxRetries + 1) + maxWaitBetweenRetries * maxRetries + 4.seconds

  def applyConfig(builder: BlazeClientBuilder[IO]): BlazeClientBuilder[IO] =
    builder.withResponseHeaderTimeout(retryTimeout)

  def backoffPolicy: Int => Option[FiniteDuration] = RetryPolicy.exponentialBackoff(
    maxWait = maxWaitBetweenRetries,
    maxRetry = maxRetries
  )
}

class RemoteTiedotuspalveluClient(config: Config) extends TiedotuspalveluClient with Logging {
  private val baseUrl = config.getString("tiedote.baseUrl")
  private val otuvaTokenEndpoint = config.getString("otuvaTokenEndpoint")

  // POSTin saa yrittää uudelleen: tiedotuspalvelu palauttaa olemassa olevan tiedotteen samalla
  // idempotencyKeyllä, ja POST vain jonottaa tiedotteen tiedotuspalvelun omalle ajastetulle lähetykselle.
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

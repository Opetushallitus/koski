package fi.oph.koski.todistus.tiedote

import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.http._
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging
import org.http4s.client.Client

case class KielitutkintoTodistusTiedoteRequest(oppijanumero: String, idempotencyKey: String)

class RemoteTiedotuspalveluClient(config: Config) extends TiedotuspalveluClient with Logging {
  private val baseUrl = config.getString("tiedote.baseUrl")
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

  private val http = oauth2clientFactory(baseUrl, Http.retryingClient("tiedotuspalvelu"))

  override def sendKielitutkintoTodistusTiedote(
    oppijanumero: String,
    idempotencyKey: String
  ): Either[HttpStatus, Unit] = {
    val request = KielitutkintoTodistusTiedoteRequest(oppijanumero, idempotencyKey)

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

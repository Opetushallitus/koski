package fi.oph.koski.todistus.swisscomclient

import cats.effect.IO
import fi.oph.koski.http.{Http, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.http.Http.{UriInterpolator, runIO}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.http4s.client.Client
import org.http4s.{Header, Headers, Request}
import org.json4s.JValue
import org.typelevel.ci.CIString

import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.util.Base64
import javax.net.ssl.{KeyManagerFactory, SSLContext}

class RemoteSwisscomClient(
  myConfig: SwisscomConfig
) extends SwisscomClient with Logging {
  val config = myConfig

  val client = new SwisscomHttpClient(config)

  protected def requestSignature(req: SwisscomAISSignRequest): Either[HttpStatus, SwisscomAISSignResponse] = {
    def parse[T](status: Int, text: String, request: Request[IO])(implicit mf : Manifest[T]): Option[T] = {
      Http.parseJsonOptional(status, text, request)
    }

    for {
      response <- runIO(
        client.client.post(uri"", req)(json4sEncoderOf[SwisscomAISSignRequest])(parse[JValue])
      )
        .map(JsonSerializer.extract[SwisscomAISSignResponse](_, ignoreExtras = true))
        .toRight(KoskiErrorCategory.internalError("No valid response from AIS"))

      result <- if (response.isSuccess) { Right(response) } else { Left(response.SignResponse.Result.toHttpStatus) }
    } yield result
  }
}

class SwisscomHttpClient(config: SwisscomConfig) extends Logging {
  private val sslContext: SSLContext = createSSLContext(config.keyStore, config.keyStorePassword)
  private val contentTypeHeader = Header.Raw(CIString("Content-Type"), "application/json;charset=UTF-8")
  private val acceptHeader = Header.Raw(CIString("Accept"), "application/json")

  // TODO: TOR-2400: Salliikohan Swisscom retryttämisen samalla RequestID:llä, vai pitäisikö retryissä generoida aina uusi?
  private val baseClient = Http.retryingClient("swisscom-client", _.withSslContext(sslContext))

  val client = {
    Http(
      config.signUrl,
      Client { request: Request[IO] =>
        baseClient.run(request.withHeaders(request.headers ++ Headers(contentTypeHeader, acceptHeader)))
      }
    )
  }

  private def createSSLContext(keystoreString: String, keystorePassword: String): SSLContext = {
    def loadStore(base64EncodedStore: String, password: String, keystore: KeyStore): Unit = {
      val inputStream = new ByteArrayInputStream(Base64.getDecoder.decode(base64EncodedStore))
      try {
        keystore.load(inputStream, password.toCharArray)
      } finally {
        inputStream.close()
      }
    }

    try {
      // TODO: TOR-2400: Käytä PKCS12, se on default nykyään? Ei tarvitse myöskään sitten konvertoida JKS:ksi asti.
      val keystore = KeyStore.getInstance("JKS")
      loadStore(keystoreString, keystorePassword, keystore)

      val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
      kmf.init(keystore, keystorePassword.toCharArray)

      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(kmf.getKeyManagers, null, null)
      sslContext
    } catch {
      case e: Exception =>
        logger.error(e)("Error setting up SSL context")
        throw new SwisscomSslException("SSL context setup failed")
    }
  }
}

class SwisscomSslException(message: String) extends Exception(message)


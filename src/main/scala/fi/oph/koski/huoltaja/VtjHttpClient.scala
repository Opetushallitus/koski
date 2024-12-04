package fi.oph.koski.huoltaja

import cats.effect.IO
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{StringToUriConverter, parseXml}
import fi.oph.koski.log.Logging
import org.http4s.client.Client
import org.http4s.{Header, Headers, Request}
import org.typelevel.ci.CIString

import java.io.ByteArrayInputStream
import java.security.{KeyStore, SecureRandom}
import java.util.Base64
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import scala.xml.Elem

class VtjSslException(message: String) extends Exception(message)

class VtjHttpClient(vtjConfig: VtjConfig) extends Logging {
  private val sslContext: SSLContext = createSSLContext(vtjConfig.keystore, vtjConfig.truststore, vtjConfig.keystorePassword)
  private val baseClient = Http.retryingClient("vtj-soap-client", _.withSslContext(sslContext))
  private val contentTypeHeader = Header.Raw(CIString("Content-Type"), "text/xml")
  private val soapActionHeader = Header.Raw(CIString("SOAPAction"), "http://tempuri.org/TeeHenkilonTunnusKysely")

  private val client = {
    Http(
      vtjConfig.serviceUrl,
      Client { request: Request[IO] =>
        baseClient.run(request.withHeaders(request.headers ++ Headers(contentTypeHeader, soapActionHeader)))
      }
    )
  }

  def postXml(xmlBody: Elem): IO[Elem] = {
    client.post("".toUri, xmlBody)(Http.Encoders.xml)(parseXml)
  }

  private def createSSLContext(keystoreString: String, truststoreString: String, keystorePassword: String): SSLContext = {
    def loadStore(base64EncodedStore: String, password: String, keystore: KeyStore): Unit = {
      val inputStream = new ByteArrayInputStream(Base64.getDecoder.decode(base64EncodedStore))
      try {
        keystore.load(inputStream, password.toCharArray)
      } finally {
        inputStream.close()
      }
    }

    try {
      val keystore = KeyStore.getInstance("PKCS12")
      loadStore(keystoreString, keystorePassword, keystore)

      val kmf = KeyManagerFactory.getInstance("SunX509")
      kmf.init(keystore, keystorePassword.toCharArray)

      val trustStore = KeyStore.getInstance("JKS")
      loadStore(truststoreString, keystorePassword, trustStore)

      val tmf = TrustManagerFactory.getInstance("SunX509")
      tmf.init(trustStore)

      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
      sslContext
    } catch {
      case e: Exception =>
        logger.error(e)("Error setting up SSL context")
        throw new VtjSslException("SSL context setup failed")
    }
  }
}

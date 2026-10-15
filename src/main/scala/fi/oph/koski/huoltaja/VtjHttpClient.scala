package fi.oph.koski.huoltaja

import cats.effect.IO
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{StringToUriConverter, parseXml}
import fi.oph.koski.log.Logging
import org.http4s.client.Client
import org.http4s.client.middleware.RetryPolicy
import org.http4s.{Header, Headers, Request}
import org.typelevel.ci.CIString

import javax.net.ssl.SSLContext
import scala.concurrent.duration.DurationInt
import scala.xml.Elem


class VtjHttpClient(serviceUrl: String, sslContext: SSLContext) extends Logging {
  // The VTJ query is a read-only lookup sent as a SOAP POST, so it is safe to retry. This absorbs stale pooled
  // connections that DVV has closed ("HTTP connection closed"), which would otherwise fail the request at once.
  private val baseClient = Http.unsafeRetryingClient(
    "vtj-soap-client",
    _.withSslContext(sslContext),
    RetryPolicy.exponentialBackoff(maxWait = 2.seconds, maxRetry = 2)
  )
  private val headers = Headers(
    Header.Raw(CIString("Content-Type"), "text/xml"),
    Header.Raw(CIString("SOAPAction"), "http://tempuri.org/TeeHenkilonTunnusKysely")
  )

  private val client = Http(serviceUrl, Client { req: Request[IO]  =>
    baseClient.run(req.withHeaders(req.headers ++ headers))
  })

  def postXml(xmlBody: Elem): IO[Elem] = {
    client.post("".toUri, xmlBody)(Http.Encoders.xml)(parseXml)
  }
}

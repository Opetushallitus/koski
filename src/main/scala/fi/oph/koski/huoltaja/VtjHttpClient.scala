package fi.oph.koski.huoltaja

import cats.effect.IO
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{StringToUriConverter, parseXml}
import fi.oph.koski.log.Logging
import org.http4s.client.Client
import org.http4s.{Header, Headers, Request}
import org.typelevel.ci.CIString

import javax.net.ssl.SSLContext
import scala.xml.Elem


class VtjHttpClient(serviceUrl: String, sslContext: SSLContext) extends Logging {
  private val baseClient = Http.retryingClient("vtj-soap-client", _.withSslContext(sslContext))
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

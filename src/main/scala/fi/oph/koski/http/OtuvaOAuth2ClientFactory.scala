package fi.oph.koski.http

import cats.effect.{IO, Resource}
import cats.effect.std.Hotswap
import fi.oph.koski.config.SecretsManager
import fi.oph.koski.log.NotLoggable
import fi.oph.scalaschema.Serializer.format
import org.http4s.{Header, Method, Request, Response, Uri, UrlForm}
import org.http4s.client.Client
import org.json4s.jackson.JsonMethods
import org.typelevel.ci.CIStringSyntax

case class OtuvaOAuth2Credentials(id: String, secret: String) extends NotLoggable

object OtuvaOAuth2Credentials {
  def fromSecretsManager: OtuvaOAuth2Credentials = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Otuva OAuth2 credentials", "OPINTOPOLKU_VIRKAILIJA_OAUTH2_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[OtuvaOAuth2Credentials](secretId)
  }
}

class OtuvaOAuth2ClientFactory(
  credentials: OtuvaOAuth2Credentials,
  otuvaTokenEndpoint: String,
) {

  private var token: Option[String] = None
  private val otuvaTokenEndpointUri = Uri.fromString(otuvaTokenEndpoint).toOption.getOrElse(throw new IllegalArgumentException(s"Invalid Otuva token endpoint URI: $otuvaTokenEndpoint"))

  def apply(serviceUrl: String, serviceClient: Client[IO]): Http = {

    def withOAuth2Token(req: Request[IO], hotswap: Hotswap[IO, Response[IO]]) = {
      getToken().flatMap(requestWithToken(req, hotswap, retry = true))
    }

    def getToken(): IO[String] = {
      synchronized(token) match {
        case Some(s) => IO.pure(s)
        case None => refreshToken()
      }
    }

    def requestWithToken(req: Request[IO], hotswap: Hotswap[IO, Response[IO]], retry: Boolean)(token: String): IO[Response[IO]] = {
      val newReq = req.putHeaders(Header.Raw(ci"Authorization", "Bearer " + token))
      hotswap.swap(serviceClient.run(newReq)).flatMap {
        // Epäonnistuvan turhan kutsun voisi joissain tapauksissa välttää tukimalla tokenin voimassaoloaikaa, mutta sitä ei tehdä yksinkertaisuuden vuoksi.
        case r: Response[IO] if r.status.code == 401 && retry =>
          refreshToken().flatMap(requestWithToken(req, hotswap, retry = false))
        case r: Response[IO] => IO.pure(r)
      }
    }

    def refreshToken(): IO[String] = {
      val body = UrlForm("grant_type" -> "client_credentials", "client_id" -> credentials.id, "client_secret" -> credentials.secret)
      val req = Request[IO](Method.POST, otuvaTokenEndpointUri).withEntity(body)

      serviceClient.run(req).use(
        res => {
          res.as[String].map {
            s =>
              val v = JsonMethods.parse(s)
              val t = (v \ "access_token").extract[String]
              synchronized {
                token = Some(t)
              }
              t
          }
        }
      )
    }

    val client = Client[IO] {
      req =>
        Hotswap.create[IO, Response[IO]].flatMap { hotswap =>
          Resource.eval(withOAuth2Token(req, hotswap))
        }
    }

    Http(serviceUrl, client)
  }

}

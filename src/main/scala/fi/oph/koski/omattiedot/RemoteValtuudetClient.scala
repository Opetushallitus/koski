package fi.oph.koski.omattiedot

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.{Base64, UUID}

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{runTask, _}
import fi.oph.koski.json.JsonSerializer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.http4s.{Header, Headers, Request}

import scala.reflect.runtime.universe.TypeTag

class RemoteValtuudetClient(config: Config) extends ValtuudetClient {
  private val host = config.getString("suomifi.valtuudet.host")
  private val clientId = config.getString("suomifi.valtuudet.clientId")
  private val apiKey = config.getString("suomifi.valtuudet.apiKey")
  private val oauthPassword = config.getString("suomifi.valtuudet.oauthPassword")

  private val http: Http = Http(host, "suomifi-valtuudet")

  private val AUTHORIZATION_HEADER = "X-AsiointivaltuudetAuthorization"

  def createSession(hetu: String): Either[ValtuudetFailure, SessionResponse] = {
    val path = s"/service/hpa/user/register/$clientId/$hetu?requestId=$requestId"
    runTask(http.get(ParameterizedUriWrapper(uriFromString(path), path), Headers(Header(AUTHORIZATION_HEADER, getChecksum(path))))(parse[SessionResponse]))
  }

  def getRedirectUrl(userId: String, callbackUrl: String, language: String): String = {
    s"$host/oauth/authorize?client_id=$clientId&response_type=code&redirect_uri=${encodeQueryParam(callbackUrl)}&user=${encodeQueryParam(userId)}&lang=${encodeQueryParam(language)}"
  }

  def getAccessToken(code: String, callbackUrl: String): Either[ValtuudetFailure, String] = {
    val requestTask = http.post(uri"/oauth/token?grant_type=authorization_code&redirect_uri=$callbackUrl&code=$code", Headers(Header("Authorization", "Basic " + getCredentials)))(parse[AccessTokenResponse])
    for {
      resp <- runTask(requestTask) match {
        case Left(status) if status.statusCode == 400 && status.responseBody.contains("invalid_grant") =>
          Left(InvalidGrant(code))
        case x => x
      }
    } yield resp.access_token
  }

  def getSelectedPerson(sessionId: String, accessToken: String): Either[ValtuudetFailure, SelectedPersonResponse] = {
    val path = s"/service/hpa/api/delegate/$sessionId?requestId=$requestId"
    val headers = Headers(
      Header(AUTHORIZATION_HEADER, getChecksum(path)),
      Header("Authorization", s"Bearer $accessToken")
    )

    runTask(http.get(ParameterizedUriWrapper(uriFromString(path), path), headers)(parse[List[SelectedPersonResponse]])).map(_.head)
  }

  private def parse[T: TypeTag](status: Int, text: String, request: Request)  = if (status == 200 || status == 201) {
    Right(JsonSerializer.parse[T](text, ignoreExtras = true))
  } else {
    Left(SuomifiValtuudetFailure(status, text, request))
  }

  private def getCredentials = {
    val credentials = clientId + ":" + oauthPassword
    Base64.getEncoder.encodeToString(credentials.getBytes(StandardCharsets.UTF_8))
  }

  private def getChecksum(path: String, instant: Instant = Instant.now) = {
    val timestamp = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)
    clientId + " " + timestamp + " " + hash(path + " " + timestamp, apiKey)
  }

  private val MAC_ALGORITHM = "HmacSHA256"
  private def hash(data: String, key: String) = {
    val mac = Mac.getInstance(MAC_ALGORITHM)
    mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), MAC_ALGORITHM))
    new String(Base64.getEncoder.encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))))
  }

  private def requestId = encodeQueryParam(UUID.randomUUID.toString)
  private def encodeQueryParam(value: String) = URLEncoder.encode(value, "UTF-8")
}

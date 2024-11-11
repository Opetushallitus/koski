package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresOmaDataOAuth2
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport



class OmaDataOAuth2AuthorizationServerServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging with ContentEncodingSupport with NoCache with FormSupport with I18nSupport with RequiresOmaDataOAuth2 with OmaDataOAuth2Support {

  // in: auth code yms. OAuth2 headerit
  // out: access token, jos käyttäjällä oikeudet kyseiseen authorization codeen.
  //      TAI OAuth2-protokollan mukainen virheilmoitus (
  //      TODO: TOR-2210 joka luotetaan nginx:n välittävän sellaisenaan, jos pyyntö on tänne asti tullut?)
  // TODO: TOR-2210 Lisävarmistus, että hyväksytään vain "application/x-www-form-urlencoded"-tyylinen input?
  post("/") {
    val result: AccessTokenResponse = validate(AccessTokenRequest.formAccessTokenRequest)(
      (errors: Seq[(String, String)]) => {
        val validationError = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, errors.map { case (a, b) => s"${a}: ${b}" }.mkString(";"))

        logger.warn(validationError.getLoggedErrorMessage)
        AccessTokenErrorResponse(validationError)
      },
      (accessTokenRequest: AccessTokenRequest) => {
        validateAccessTokenRequest(accessTokenRequest) match {
          case Left(validationError) =>

            logger.warn(validationError.getLoggedErrorMessage)
            AccessTokenErrorResponse(validationError)
          case _ =>
            // TODO: TOR-2210 Oikea toteutus ja yksikkötestit
            // Bearer-token spec: https://www.rfc-editor.org/rfc/rfc6750

            AccessTokenSuccessResponse(
              access_token = "dummy-access-token",
              token_type = "Bearer",
              expires_in = 600
            )
        }
      }
    )

    response.setStatus(result.httpStatus)
    renderObject(result)
  }

  private def validateAccessTokenRequest(request: AccessTokenRequest): Either[OmaDataOAuth2Error, Unit] = {
    for {
      _ <- validateClientId(request.client_id)
    } yield ()
  }

  private def validateClientId(clientIdParam: String): Either[OmaDataOAuth2Error, String] = {
    for {
      clientId <- validateClientIdRekisteröity(clientIdParam)
      _ <- validateClientIdSamaKuinKäyttäjätunnus(clientId)
    } yield clientId
  }

  private def validateClientIdSamaKuinKäyttäjätunnus(clientId: String): Either[OmaDataOAuth2Error, String] = {
    if (koskiSession.user.username == clientId) {
      Right(clientId)
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_client_data, s"Annettu client_id ${clientId} on eri kuin mTLS-käyttäjä ${koskiSession.user.username}"))
    }
  }
}
object AccessTokenRequest {
  val formAccessTokenRequest: MappingValueType[AccessTokenRequest] = mapping(
    "grant_type" -> label("grant_type", text(required, oneOf(Seq("authorization_code")))),
    "code" -> label("code", text(required, oneOf(Seq("foobar")))), // TODO: TOR-2210 Oikea koodi-patternin validointi?
    "code_verifier" -> label("code_verifier", text(required)),
    "redirect_uri" -> label("redirect_uri", optional(text())), // TODO: TOR-2210 Vertaa, että tämän sisältö on täsmälleen sama kuin alkuperäisessä pyynnössä, jos siellä on redirect_uri annettu
    "client_id" -> label("client_id", text(required))
  )(AccessTokenRequest.apply)
}

case class AccessTokenRequest(
  grant_type: String,
  code: String,
  code_verifier: String,
  redirect_uri: Option[String],
  client_id: String
)

trait AccessTokenResponse {
  def httpStatus: Int
}

case class AccessTokenSuccessResponse(
  access_token: String,
  token_type: String,
  expires_in: Long,
  // refresh_token: Option[String] // TODO: TOR-2210: jos tarvitaan refresh token
  // scope: Option[String] // TOD: TOR-2210: jos aletaan joskus tukea scopen vaihtoa tässä vaiheessa
) extends AccessTokenResponse {
  val httpStatus = 200
}

object AccessTokenErrorResponse {
  def apply(validationError: OmaDataOAuth2Error): AccessTokenErrorResponse = {
    AccessTokenErrorResponse(
      "invalid_client",
      Some(validationError.getLoggedErrorMessage),
      None
    )
  }
}

case class AccessTokenErrorResponse(
  error: String,
  error_description: Option[String],
  error_uri: Option[String]
) extends AccessTokenResponse {
  val httpStatus = 400
}

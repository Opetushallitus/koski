package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresOmaDataOAuth2
import fi.oph.koski.log.Logging
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.challengeFromVerifier
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

import java.time.Instant
import java.time.temporal.ChronoUnit

class OmaDataOAuth2AuthorizationServerServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging with ContentEncodingSupport with NoCache with FormSupport with I18nSupport with RequiresOmaDataOAuth2 with OmaDataOAuth2Support {

  // in: auth code yms. OAuth2 headerit
  // out: access token, jos käyttäjällä oikeudet kyseiseen authorization codeen
  //        TAI OAuth2-protokollan mukainen virheilmoitus (
  post("/") {
    val result: AccessTokenResponse = validate(AccessTokenRequest.formAccessTokenRequest)(
      (errors: Seq[(String, String)]) => {
        val validationError = OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, errors.map { case (a, b) => s"${a}: ${b}" }.mkString(";"))

        logger.warn(validationError.getLoggedErrorMessage)
        validationError.getAccessTokenErrorResponse
      },
      (accessTokenRequest: AccessTokenRequest) => {
        validateAccessTokenRequest(accessTokenRequest) match {
          case Left(validationError) =>

            logger.warn(validationError.getLoggedErrorMessage)
            validationError.getAccessTokenErrorResponse
          case _ =>
            application.omaDataOAuth2Service.createAccessTokenForCode(
              code = accessTokenRequest.code,
              expectedClientId = accessTokenRequest.client_id,
              expectedCodeChallenge = challengeFromVerifier(accessTokenRequest.code_verifier),
              expectedRedirectUri = accessTokenRequest.redirect_uri,
              koskiSession = koskiSession,
              allowedScopes = koskiSession.omaDataOAuth2Scopes
            ) match {
              case Left(error) => error.getAccessTokenErrorResponse
              case Right(accessTokenInfo: AccessTokenInfo) => AccessTokenSuccessResponse(
                access_token = accessTokenInfo.accessToken,
                token_type = "Bearer",
                expires_in = Instant.now().until(accessTokenInfo.expirationTime, ChronoUnit.SECONDS)
              )
            }
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
      clientId <- validateClientIdRekisteröity(clientIdParam, OmaDataOAuth2ErrorType.invalid_client)
      _ <- validateClientIdSamaKuinKäyttäjätunnus(clientId)
    } yield clientId
  }

  private def validateClientIdSamaKuinKäyttäjätunnus(clientId: String): Either[OmaDataOAuth2Error, String] = {
    if (koskiSession.user.username == clientId) {
      Right(clientId)
    } else {
      Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_client, s"Annettu client_id ${clientId} on eri kuin mTLS-käyttäjä ${koskiSession.user.username}"))
    }
  }
}
object AccessTokenRequest {
  val formAccessTokenRequest: MappingValueType[AccessTokenRequest] = mapping(
    "grant_type" -> label("grant_type", text(required, oneOf(Seq("authorization_code")))),
    "code" -> label("code", text(required)),
    "code_verifier" -> label("code_verifier", text(required)),
    "redirect_uri" -> label("redirect_uri", optional(text())),
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

case class AccessTokenErrorResponse(
  error: String,
  error_description: Option[String],
  error_uri: Option[String]
) extends AccessTokenResponse {
  val httpStatus = 400
}

package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresOmaDataOAuth2
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport



class OmaDataOAuth2AuthorizationServerServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging with ContentEncodingSupport with NoCache with FormSupport with I18nSupport with RequiresOmaDataOAuth2
{

  // in: auth code yms. OAuth2 headerit
  // out: access token, jos käyttäjällä oikeudet kyseiseen authorization codeen.
  //      TAI OAuth2-protokollan mukainen virheilmoitus (joka luotetaan nginx:n välittävän sellaisenaan, jos pyyntö on tänne asti tullut?)
  // TODO: Lisävarmistus, että hyväksytään vain "application/x-www-form-urlencoded"-tyylinen input?
  post("/") {
    val result = validate(AccessTokenRequest.formAccessTokenRequest)(
      (errors: Seq[(String, String)]) => {
        // TODO: OAuth2-standardin mukaiset virheet
        Left(KoskiErrorCategory.badRequest(errors.map { case (a, b) => s"${a}: ${b}" }.mkString(";")))
      },
      (accessTokenRequest: AccessTokenRequest) => {
        // TODO: Oikea toteutus ja yksikkötestit
        // Bearer-token spec: https://www.rfc-editor.org/rfc/rfc6750
        Right(AccessTokenResponse(access_token = "dummy-access-token", token_type = "Bearer", expires_in = 86400))
      }
    )

    renderEither(result)
  }
}

object AccessTokenRequest {
  // TODO: Voiko tehdä omia dynaaamisia constrainteja, jotka tsekkaa muitakin asioita, esim. tietokannasta asti?
  val formAccessTokenRequest: MappingValueType[AccessTokenRequest] = mapping(
    "grant_type" -> label("grant_type", text(required, oneOf(Seq("authorization_code")))),
    "code" -> label("code", text(required, oneOf(Seq("foobar")))), // TODO: Oikea koodi-patternin validointi?
    "redirect_uri" -> label("redirect_uri", optional(text())), // TODO: URI pattern matching? Oikeasti tarve on verrata, että tämä täsmälleen sama, millä on luotu
    "client_id" -> label("client_id", optional(text())) // TODO: Tsekkaa, että vastaa käyttäjää, jolla tultiin sisään, jos on annettu?
  )(AccessTokenRequest.apply)
}

case class AccessTokenRequest(
  grant_type: String,
  code: String,
  redirect_uri: Option[String],
  client_id: Option[String]
)

case class AccessTokenResponse(
  access_token: String,
  token_type: String,
  expires_in: Long,
  // refresh_token: Option[String] // jos tarvitaan refresh token
  // scope: Option[String] // jos aletaan joskus tukea scopen vaihtoa tässä vaiheessa
)

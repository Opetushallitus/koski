package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.http4s.Uri
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URLEncoder
import java.util.Base64

class OmaDataOAuth2TestBase extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  protected def queryStringUrlEncode(str: String): String = URLEncoder.encode(str, "UTF-8").replace("+", "%20")
  protected def base64UrlEncode(str: String): String = Base64.getUrlEncoder().encodeToString(str.getBytes("UTF-8"))
  protected def base64UrlDecode(str: String): String = new String(Base64.getUrlDecoder().decode(str), "UTF-8")

  val authorizeFrontendBaseUri = "omadata-oauth2/authorize"
  val authorizeBaseUri = "api/omadata-oauth2/resource-owner/authorize"
  val postResponseBaseUri = "omadata-oauth2/post-response"

  val validDummyCode = "foobar"

  val hetu = KoskiSpecificMockOppijat.eero.hetu.get
  val oppijaOid = KoskiSpecificMockOppijat.eero.oid

  // https://datatracker.ietf.org/doc/html/rfc7636#appendix-B
  val validDummyCodeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
  val validDummyCodeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

  val validClientId = "oauth2client"
  val validState = "internal state"
  val validRedirectUri = "/koski/omadata-oauth2/debug-post-response"

  val validScope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

  val validAuthorizeParams: Seq[(String, String)] = Seq(
    ("client_id", validClientId),
    ("response_type", "code"),
    ("response_mode", "form_post"),
    ("redirect_uri", validRedirectUri),
    ("code_challenge", validDummyCodeChallenge),
    ("code_challenge_method", "S256"),
    ("state", validState),
    ("scope", validScope)
  )

  val validAuthorizeParamsString = createParamsString(validAuthorizeParams)

  def validParamsIlman(paramName: String): Seq[(String, String)] = {
    (validAuthorizeParams.toMap - paramName).toSeq
  }

  def validParamsDuplikaatilla(paramName: String): Seq[(String, String)] = {
    val duplikaatti = validAuthorizeParams.toMap.get(paramName).get
    validAuthorizeParams ++ Seq((paramName, duplikaatti))
  }

  def validParamsVaihdetullaArvolla(paramName: String, value: String): Seq[(String, String)] = {
    (validAuthorizeParams.toMap + (paramName -> value)).toSeq
  }

  def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${queryStringUrlEncode(value)}"
  }.mkString("&")

  def encodedParamStringShouldContain(base64UrlEncodedParams: String, expectedParams: Seq[(String, String)]) = {
    val actualParamsString = base64UrlDecode(base64UrlEncodedParams)
    val actualParams = Uri.unsafeFromString("/?" + actualParamsString).params

    actualParams should contain allElementsOf (expectedParams)
  }

  def getFromEncodedParamString(base64UrlEncodedParams: String, key: String): Option[String] = {
    val actualParamsString = base64UrlDecode(base64UrlEncodedParams)
    val actualParams = Uri.unsafeFromString("/?" + actualParamsString).params

    actualParams.get(key)
  }
}

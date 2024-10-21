package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.KoskiHttpSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URLEncoder
import java.util.Base64

class OmaDataOAuth2TestBase extends AnyFreeSpec with KoskiHttpSpec with Matchers {

  val validDummyCode = "foobar"

  // https://datatracker.ietf.org/doc/html/rfc7636#appendix-B
  val validDummyCodeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
  val validDummyCodeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

  protected def queryStringUrlEncode(str: String): String = URLEncoder.encode(str, "UTF-8").replace("+", "%20")
  protected def base64UrlEncode(str: String): String = Base64.getUrlEncoder().encodeToString(str.getBytes("UTF-8"))
  protected def base64UrlDecode(str: String): String = new String(Base64.getUrlDecoder().decode(str), "UTF-8")

  protected def createParamsString(params: Seq[(String, String)]): String = params.map {
    case (name, value) => s"${name}=${queryStringUrlEncode(value)}"
  }.mkString("&")
}

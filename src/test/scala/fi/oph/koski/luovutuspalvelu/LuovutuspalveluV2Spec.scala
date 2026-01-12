package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.kela.KelaRequest
import org.scalatest.freespec.AnyFreeSpec

class LuovutuspalveluV2Spec extends AnyFreeSpec with KoskiHttpSpec {

  "Kelan yhden oppijan rajapinta" - {
    "Toimii LuovutuspalveluV2 headerilla" in {
      post(
        "api/luovutuspalvelu/kela/hetu",
        JsonSerializer.writeWithRoot(KelaRequest(KoskiSpecificMockOppijat.amis.hetu.get)),
        headers = mockLuovutuspalveluV2KelaHeader ++ jsonContent
      ) {
        verifyResponseStatusOk()
      }
    }

    "Palauttaa virheen väärällä headerilla" in {
      post("api/luovutuspalvelu/kela/hetu",
        JsonSerializer.writeWithRoot(KelaRequest(KoskiSpecificMockOppijat.amis.hetu.get)),
        headers = mockLuovutuspalveluV2InvalidHeader ++ jsonContent) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized("Tuntematon varmenne"))
      }
    }

    "Palauttaa virheen väärällä IP:llä" in {
      post("api/luovutuspalvelu/kela/hetu",
        JsonSerializer.writeWithRoot(KelaRequest(KoskiSpecificMockOppijat.amis.hetu.get)),
        headers = mockLuovutuspalveluV2KelaInvalidIpHeader ++ jsonContent) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized("Tuntematon IP-osoite"))
      }
    }

    "Palauttaa virheen estetyllä varmenteen myöntäjällä" in {
      post("api/luovutuspalvelu/kela/hetu",
        JsonSerializer.writeWithRoot(KelaRequest(KoskiSpecificMockOppijat.amis.hetu.get)),
        headers = mockLuovutuspalveluV2DisallowedIssuerHeader ++ jsonContent) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized("Virheellinen varmenteen myöntäjä"))
      }
    }
  }

  def mockLuovutuspalveluV2KelaHeader: Headers = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=kela",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
    "X-Forwarded-For" -> "0.0.0.0"
  )

  def mockLuovutuspalveluV2KelaInvalidIpHeader: Headers = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=kela",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
    "X-Forwarded-For" -> "255.255.255.255"
  )

  def mockLuovutuspalveluV2InvalidHeader: Headers = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=example.com",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
    "X-Forwarded-For" -> "0.0.0.0"
  )

  def mockLuovutuspalveluV2DisallowedIssuerHeader: Headers = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=kela",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=ip-10-0-0-1.eu-west-1.compute.internal",
    "X-Forwarded-For" -> "0.0.0.0"
  )
}

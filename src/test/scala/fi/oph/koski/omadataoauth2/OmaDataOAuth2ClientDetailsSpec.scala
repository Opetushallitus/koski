package fi.oph.koski.omadataoauth2

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class OmaDataOAuth2ClientDetailsSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  val app = KoskiApplicationForTests

  val validClientId = "oauth2client"

  "client-details route" - {
    val clientDetailsUri = s"api/omadata-oauth2/resource-owner/client-details/${validClientId}"

    val hetu = KoskiSpecificMockOppijat.eero.hetu.get

    "Palauttaa 401, jos kansalainen ei ole kirjautunut" in {
      get(
        uri = clientDetailsUri
      ) {
        verifyResponseStatus(401)
      }
    }

    "Palauttaa vastauksen kirjautuneena" in {
      get(
        uri = clientDetailsUri,
        headers = kansalainenLoginHeaders(hetu)
      ) {
        verifyResponseStatusOk()
        // TODO: TOR-2210 vastauksen sisältö
      }
    }
  }
}




package fi.oph.koski.omadataoauth2

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets

class OmaDataOAuth2Spec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  val app = KoskiApplicationForTests

  "authorization-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      postAuthorizationServer(MockUsers.omadataOAuth2Palvelukäyttäjä) {
        verifyResponseStatusOk()
      }
    }
    "ei voi kutsua ilman käyttöoikeuksia" in {
      postAuthorizationServer(MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
      }
    }
  }


  "resource-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      postResourceServer(MockUsers.omadataOAuth2Palvelukäyttäjä) {
        verifyResponseStatusOk()
      }

    }
    "ei voi kutsua ilman käyttöoikeuksia" in {
      postResourceServer(MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
      }
    }
  }

  private def postAuthorizationServer[T](user: KoskiMockUser, grantType: String = "authorization_code", code: String = "foobar")(f: => T): T = {
    post(uri = "api/omadata-oauth2/authorization-server",
      body = createFormParametersBody(grantType, code),
      headers = authHeaders(user) ++ formContent)(f)
  }

  private def createFormParametersBody(grantType: String, code: String): Array[Byte] = {
    s"grant_type=${grantType}&code=${code}".getBytes(StandardCharsets.UTF_8)
  }

  private def postResourceServer[T](user: KoskiMockUser, token: String = "dummy-access-token")(f: => T): T = {
    val tokenHeaders = Map("X-Auth" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = authHeaders(user) ++ tokenHeaders)(f)
  }
}



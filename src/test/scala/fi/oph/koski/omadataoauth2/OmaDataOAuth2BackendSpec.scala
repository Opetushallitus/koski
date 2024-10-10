package fi.oph.koski.omadataoauth2

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}

import java.nio.charset.StandardCharsets

class OmaDataOAuth2BackendSpec extends OmaDataOAuth2TestBase {
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

    "redirect_uri" - {
      "vaaditaan, että on sama, jos oli annettu myös autorisointikutsussa" in {
        // TODO: TOR-2210
      }

      "ei vaadita, jos ei ollut mukana autorisointikutsussa" in {
        // TODO: TOR-2210, jos tätä polkua tuetaan
      }
    }

    "kun optionaalinen client_id on annettu" - {
      "voi kutsua, kun client on rekisteröity" in {
        val user = MockUsers.omadataOAuth2Palvelukäyttäjä
        postAuthorizationServer(user, clientId = Some(user.username)) {
          verifyResponseStatusOk()
        }
      }
      "ei voi kutsua, kun clientia ei ole rekisteröity" in {
        val user = MockUsers.rekisteröimätönOmadataOAuth2Palvelukäyttäjä
        postAuthorizationServer(user, clientId = Some(user.username)) {
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2
          verifyResponseStatus(400)
        }
      }

      "ei voi kutsua, jos rekisteröity client_id ei vastaa käyttäjätunnusta" in {
        val user = MockUsers.omadataOAuth2Palvelukäyttäjä
        val vääräUser = MockUsers.rekisteröimätönOmadataOAuth2Palvelukäyttäjä
        postAuthorizationServer(user, clientId = Some(vääräUser.username)) {
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2
          verifyResponseStatus(400)
        }
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


  private def postAuthorizationServer[T](user: KoskiMockUser, grantType: String = "authorization_code", code: String = validDummyCode, clientId: Option[String] = None)(f: => T): T = {
    post(uri = "api/omadata-oauth2/authorization-server",
      body = createFormParametersBody(grantType, code, clientId),
      headers = authHeaders(user) ++ formContent)(f)
  }

  private def createFormParametersBody(grantType: String, code: String, client_id: Option[String]): Array[Byte] = {
    val params = Seq(
      ("grant_type", grantType),
      ("code", code),
    ) ++ client_id.toSeq.map(clientId => ("client_id", clientId))

    createParamsString(params).getBytes(StandardCharsets.UTF_8)
  }

  private def postResourceServer[T](user: KoskiMockUser, token: String = "dummy-access-token")(f: => T): T = {
    val tokenHeaders = Map("X-Auth" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = authHeaders(user) ++ tokenHeaders)(f)
  }
}



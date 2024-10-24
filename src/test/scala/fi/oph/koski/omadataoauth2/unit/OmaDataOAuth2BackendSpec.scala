package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}

import java.nio.charset.StandardCharsets

class OmaDataOAuth2BackendSpec extends OmaDataOAuth2TestBase {
  "authorization-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      postAuthorizationServerClientIdFromUsername(MockUsers.omadataOAuth2Palvelukäyttäjä) {
        verifyResponseStatusOk()
      }
    }
    "ei voi kutsua ilman käyttöoikeuksia" in {
      postAuthorizationServerClientIdFromUsername(MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
      }
    }

    "redirect_uri" - {
      "vaaditaan, jos oli mukana autorisointikutsussa" in {
        // TODO: TOR-2210
      }

      "vaaditaan, että on sama, jos oli annettu myös autorisointikutsussa" in {
        // TODO: TOR-2210
      }

      "ei vaadita, jos ei ollut mukana autorisointikutsussa" in {
        // TODO: TOR-2210, jos tätä polkua tuetaan
      }
    }

    "grant_type" - {
      "puuttuminen aiheuttaa virheen" in {
        postAuthorizationServerClientIdFromUsername(MockUsers.omadataOAuth2Palvelukäyttäjä, grantType = None) {
          verifyResponseStatus(400)
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2

        }
      }
      "muu arvo kuin authorization_code aiheuttaa virheen" in {
        postAuthorizationServerClientIdFromUsername(MockUsers.omadataOAuth2Palvelukäyttäjä, grantType = Some("ei_tuettu")) {
          verifyResponseStatus(400)
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2
        }
      }
    }

    "client_id" - {
      "puuttuminen aiheuttaa virheen" in {
        postAuthorizationServer(MockUsers.omadataOAuth2Palvelukäyttäjä, clientId = None) {
          verifyResponseStatus(400)
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2

        }
      }
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

    "code" - {
      "puuttuminen aiheuttaa virheen" in {
        postAuthorizationServerClientIdFromUsername(MockUsers.omadataOAuth2Palvelukäyttäjä, code = None) {
          verifyResponseStatus(400)
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2
        }
      }

      "tuntematon koodi aiheuttaa virheen" in {
        // TODO: TOR-2210
      }
    }

    "code_verifier" - {
      "puuttuminen aiheuttaa virheen" in {
        postAuthorizationServerClientIdFromUsername(MockUsers.omadataOAuth2Palvelukäyttäjä, codeVerifier = None) {
          verifyResponseStatus(400)
          // TODO: TOR-2210: oikeat error-koodit ja sisällöt, https://www.rfc-editor.org/rfc/rfc6749#section-5.2
        }
      }

      "tarkistetaan" in {
        // TODO: TOR-2210
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

  private def postAuthorizationServerClientIdFromUsername[T](
    user: KoskiMockUser,
    grantType: Option[String] = Some("authorization_code"),
    code: Option[String] = Some(validDummyCode),
    codeVerifier: Option[String] = Some(validDummyCodeVerifier),
    redirectUri: Option[String] = None)(f: => T): T =
  {
    val clientId = Some(user.username)
    postAuthorizationServer(user, clientId, grantType, code, codeVerifier, redirectUri)(f)
  }

  private def postAuthorizationServer[T](
    user: KoskiMockUser,
    clientId: Option[String],
    grantType: Option[String] = Some("authorization_code"),
    code: Option[String] = Some(validDummyCode),
    codeVerifier: Option[String] = Some(validDummyCodeVerifier),
    redirectUri: Option[String] = None)(f: => T): T =
  {
    post(uri = "api/omadata-oauth2/authorization-server",
      body = createFormParametersBody(grantType, code, codeVerifier, clientId, redirectUri),
      headers = authHeaders(user) ++ formContent)(f)
  }

  private def createFormParametersBody(grantType: Option[String], code: Option[String], codeVerifier: Option[String], clientId: Option[String], redirectUri: Option[String]): Array[Byte] = {
    val params =
      grantType.toSeq.map(v => ("grant_type", v)) ++
      code.toSeq.map(v => ("code", v)) ++
      codeVerifier.toSeq.map(v => ("code_verifier", v)) ++
      clientId.toSeq.map(v => ("client_id", v)) ++
      redirectUri.toSeq.map(v => ("redirect_uri", v))

    createParamsString(params).getBytes(StandardCharsets.UTF_8)
  }

  private def postResourceServer[T](user: KoskiMockUser, token: String = "dummy-access-token")(f: => T): T = {
    val tokenHeaders = Map("X-Auth" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = authHeaders(user) ++ tokenHeaders)(f)
  }
}



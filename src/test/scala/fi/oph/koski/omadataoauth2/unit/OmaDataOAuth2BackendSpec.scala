package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.DatabaseTestMethods
import fi.oph.koski.db.KoskiTables.OAuth2JakoKaikki
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.{AccessTokenErrorResponse, AccessTokenSuccessResponse}
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.{createChallengeAndVerifier, sha256}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

import java.sql.Timestamp
import java.time.Instant

class OmaDataOAuth2BackendSpec extends OmaDataOAuth2TestBase with DatabaseTestMethods {
  "authorization-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      val pkce = createChallengeAndVerifier
      val code = createAuthorization(validKansalainen, pkce.challenge)

      postAuthorizationServerClientIdFromUsername(
        validPalvelukäyttäjä,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatusOk()
      }
    }
    "tekee audit-lokimerkinnän" in {
      val pkce = createChallengeAndVerifier
      val code = createAuthorization(validKansalainen, pkce.challenge)

      AuditLogTester.clearMessages()
      postAuthorizationServerClientIdFromUsername(
        validPalvelukäyttäjä,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatusOk()

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OAUTH2_ACCESS_TOKEN_LUONTI",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "omaDataKumppani" -> validClientId,
            "omaDataOAuth2Scope" -> validScope
          ),
        ))
      }
    }

    "ei voi kutsua ilman käyttöoikeuksia" in {
      val pkce = createChallengeAndVerifier
      val code = createAuthorization(validKansalainen, pkce.challenge)

      postAuthorizationServerClientIdFromUsername(
        MockUsers.kalle,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
      }
    }

    "redirect_uri" - {
      "eri URI kuin autorisointikutussa aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of non-matching redirect_uri")
        }
      }
      "väärän URIn käyttöyritys sulkee koodin" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of non-matching redirect_uri")
        }

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }
    }

    "grant_type" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          grantType = None
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("grant_type is required")
        }
      }
      "muu arvo kuin authorization_code aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          grantType = Some("ei_tuettu")
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("grant_type must be one of 'authorization_code'")
        }
      }
    }

    "client_id" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = None
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("client_id is required")
        }
      }
      "voi kutsua, kun client on rekisteröity" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val user = MockUsers.omadataOAuth2Palvelukäyttäjä
        postAuthorizationServer(user,
          clientId = Some(user.username),
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatusOk()
        }
      }
      "ei voi kutsua, kun clientia ei ole rekisteröity" in {
        val user = MockUsers.rekisteröimätönOmadataOAuth2Palvelukäyttäjä

        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          user,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = Some(user.username)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_client")
          result.error_description.get should include("unregistered client oauth2clienteirek")
        }
      }

      "ei voi kutsua, jos rekisteröity client_id ei vastaa käyttäjätunnusta" in {
        val user = MockUsers.omadataOAuth2Palvelukäyttäjä
        val vääräUser = MockUsers.rekisteröimätönOmadataOAuth2Palvelukäyttäjä

        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          user,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = Some(vääräUser.username)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_client")
          result.error_description.get should include("unregistered client oauth2clienteirek")
        }
      }
    }

    "scope" - {
      "palauttaa virheen, jos scope on liian laaja" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge, validScope + " HENKILOTIEDOT_HETU")

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_scope")
          result.error_description.get should include(s"scope=HENKILOTIEDOT_HETU exceeds the rights granted to the client ${validClientId}")
        }
      }

      "liian laajan scopen käyttöyritys ei sulje koodia kokonaan" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge, validScope + " HENKILOTIEDOT_HETU")

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_scope")
          result.error_description.get should include(s"scope=HENKILOTIEDOT_HETU exceeds the rights granted to the client ${validClientId}")
        }

        // Varmista, että sama virhe tulee uudestaan. Jos koodi olisi kokonaan suljettu, ei koodia löytyisi lainkaan.
        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_scope")
          result.error_description.get should include(s"scope=HENKILOTIEDOT_HETU exceeds the rights granted to the client ${validClientId}")
        }
      }
    }

    "code" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = None,
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("code is required")
        }
      }

      "tuntematon koodi aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge) + "ERROR"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "ei toimi, jos mitätöity" in {
        // Triggeröi mitätöinti käyttämällä ensin väärää redirect_uri:a
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of non-matching redirect_uri")
        }

        // Uusi yritys oikealla redirect_uri:lla
        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "ei toimi, jos vanhentunut" in {
        // Luo koodi
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatusOk()
        }

        // Vaihda aikaleima menneisyyteen
        runDbSync(
          OAuth2JakoKaikki
            .filter(_.codeSHA256 === sha256(code))
            .map(_.codeVoimassaAsti)
            .update(
              Timestamp.from(Instant.now.minusSeconds(1))
            )
        )

        // Yritä käyttää
        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "saman koodin uudelleenkäyttöyritys" - {
        "palauttaa virheen" in {
          val pkce = createChallengeAndVerifier
          val code = createAuthorization(validKansalainen, pkce.challenge)

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatusOk()
          }

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Attempted authorization code reuse")
          }
        }
        "sulkee koodin" in {
          val pkce = createChallengeAndVerifier
          val code = createAuthorization(validKansalainen, pkce.challenge)

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatusOk()
          }

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Attempted authorization code reuse")
          }

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Code not found or it has expired")
          }
        }
        "sulkee access tokenin" in {
          val pkce = createChallengeAndVerifier
          val code = createAuthorization(validKansalainen, pkce.challenge)

          val token = postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatusOk()
            JsonSerializer.parse[AccessTokenSuccessResponse](response.body).access_token
          }

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Attempted authorization code reuse")
          }

          postResourceServer(token) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Access token not found or it has expired")
          }
        }
      }
    }

    "code_verifier" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = None,
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("code_verifier is required")
        }
      }

      "tarkistetaan" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidVerifier = pkce.verifier + "123"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(invalidVerifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of invalid code_verifier")
        }
      }

      "väärän verifierin käyttö sulkee koodin" in {
        val pkce = createChallengeAndVerifier
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidVerifier = pkce.verifier + "123"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(invalidVerifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of invalid code_verifier")
        }

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }
    }
  }

  "resource-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      val pkce = createChallengeAndVerifier
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token) {
        verifyResponseStatusOk()
      }
    }

    "tekee audit log -merkinnän" in {
      val pkce = createChallengeAndVerifier
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      AuditLogTester.clearMessages()

      postResourceServer(token) {
        verifyResponseStatusOk()

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "omaDataKumppani" -> validClientId,
            "omaDataOAuth2Scope" -> validScope
          ),
        ))
      }
    }

    "ei voi kutsua ilman käyttöoikeuksia" in {
      val pkce = createChallengeAndVerifier
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token, MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
      }
    }
    "ei voi kutsua, jos liian laaja scope" in {
      // palvelukäyttäjän käyttöoikeudet voivat muuttua myös tokenin elinaikana, siksi tämä on tärkeä testata
      val pkce = createChallengeAndVerifier
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      // Vaihda scope laajemmaksi
      runDbSync(
        OAuth2JakoKaikki
          .filter(_.accessTokenSHA256 === sha256(token))
          .map(_.scope)
          .update(
            validScope + " HENKILOTIEDOT_KAIKKI_TIEDOT"
          )
      )

      // Yritä käyttää
      postResourceServer(token) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
        result.error should be("invalid_scope")
        result.error_description.get should include("scope=HENKILOTIEDOT_KAIKKI_TIEDOT exceeds the rights granted to the client oauth2client")
      }
    }
    "ei voi kutsua, kun token on vanhentunut" in {
      val pkce = createChallengeAndVerifier
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      // Vaihda aikaleima menneisyyteen
      runDbSync(
        OAuth2JakoKaikki
          .filter(_.accessTokenSHA256 === sha256(token))
          .map(_.voimassaAsti)
          .update(
            Timestamp.from(Instant.now.minusSeconds(1))
          )
      )

      // Yritä käyttää
      postResourceServer(token) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[AccessTokenErrorResponse](response.body)
        result.error should be("invalid_request")
        result.error_description.get should include("Access token not found or it has expired")
      }
    }
    "henkilötiedot" - {
      "palautetaan scopen mukaan" in {
        // TODO: TOR-2210
      }
    }

    "suoritetut tutkinnot" - {
      "palautetaan scopen mukaan" in {
        // TODO: TOR-2210
      }
    }
    "aktiiviset opinnot" - {
      "palautetaan scopen mukaan" in {
        // TODO: TOR-2210
      }
    }
  }

  private def postResourceServer[T](token: String, user: KoskiMockUser = validPalvelukäyttäjä)(f: => T): T = {
    val tokenHeaders = Map("X-Auth" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = authHeaders(user) ++ tokenHeaders)(f)
  }
}



package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus
import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, OpiskeluoikeusTestMethodsPerusopetus, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.db.KoskiTables.OAuth2JakoKaikki
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.fixture.{FixtureCreator, PerusopetuksenOpiskeluoikeusTestData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.{createChallengeAndVerifier, sha256}
import fi.oph.koski.omadataoauth2._
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, PerusopetuksenOpiskeluoikeudenLisätiedot, PerusopetuksenOpiskeluoikeus}
import fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenOpiskeluoikeus
import fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVerifiers
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotVerifiers
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, schema}

import java.sql.Timestamp
import java.time.Instant

class OmaDataOAuth2BackendSpec
  extends OmaDataOAuth2TestBase
    with DatabaseTestMethods
    with OpiskeluoikeusTestMethods
    with SuoritetutTutkinnotVerifiers
    with AktiivisetJaPäättyneetOpinnotVerifiers
    with OpiskeluoikeusTestMethodsPerusopetus
{
  "authorization-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      val pkce = createChallengeAndVerifier()
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
    "voi kutsua, kun on käyttöoikeudet OPH:lla" in {
      val user = MockUsers.omadataOAuth2OphPalvelukäyttäjä
      val pkce = createChallengeAndVerifier()
      val code = createAuthorization(kansalainen = validKansalainen, codeChallenge = pkce.challenge, user = user)

      postAuthorizationServerClientIdFromUsername(
        user,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatusOk()
      }
    }
    "tekee audit-lokimerkinnän" in {
      val pkce = createChallengeAndVerifier()
      val code = createAuthorization(validKansalainen, pkce.challenge)

      AuditLogTester.clearMessages()
      postAuthorizationServerClientIdFromUsername(
        validPalvelukäyttäjä,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatusOk()

        AuditLogTester.verifyOnlyAuditLogMessage(Map(
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
      val pkce = createChallengeAndVerifier()
      val code = createAuthorization(validKansalainen, pkce.challenge)

      postAuthorizationServerClientIdFromUsername(
        MockUsers.kalle,
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatus(403)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_client")
      }
    }

    "redirect_uri" - {
      "eri URI kuin autorisointikutussa aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of non-matching redirect_uri")
        }
      }
      "väärän URIn käyttöyritys sulkee koodin" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
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
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }
    }

    "grant_type" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          grantType = None
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("grant_type is required")
        }
      }
      "muu arvo kuin authorization_code aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          grantType = Some("ei_tuettu")
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("grant_type must be one of 'authorization_code'")
        }
      }
    }

    "client_id" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = None
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("client_id is required")
        }
      }
      "voi kutsua, kun client on rekisteröity" in {
        val pkce = createChallengeAndVerifier()
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

        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          user,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = Some(user.username)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_client")
          result.error_description.get should include("unregistered client oauth2clienteirek")
        }
      }

      "ei voi kutsua, jos rekisteröity client_id ei vastaa käyttäjätunnusta" in {
        val user = MockUsers.omadataOAuth2Palvelukäyttäjä
        val vääräUser = MockUsers.rekisteröimätönOmadataOAuth2Palvelukäyttäjä

        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServer(
          user,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri),
          clientId = Some(vääräUser.username)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_client")
          result.error_description.get should include("unregistered client oauth2clienteirek")
        }
      }
    }

    "scope" - {
      "palauttaa virheen, jos scope on liian laaja" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge, validScope + " HENKILOTIEDOT_HETU")

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_scope")
          result.error_description.get should include(s"scope=HENKILOTIEDOT_HETU exceeds the rights granted to the client ${validClientId}")
        }
      }

      "liian laajan scopen käyttöyritys ei sulje koodia kokonaan" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge, validScope + " HENKILOTIEDOT_HETU")

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
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
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_scope")
          result.error_description.get should include(s"scope=HENKILOTIEDOT_HETU exceeds the rights granted to the client ${validClientId}")
        }
      }
    }

    "code" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = None,
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("code is required")
        }
      }

      "tuntematon koodi aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge) + "ERROR"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "ei toimi, jos mitätöity" in {
        // Triggeröi mitätöinti käyttämällä ensin väärää redirect_uri:a
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidRedirectUri = "/koski/omadata-oauth2/INVALID-debug-post-response"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(pkce.verifier),
          redirectUri = Some(invalidRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
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
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "ei toimi, jos vanhentunut" in {
        // Luo koodi
        val pkce = createChallengeAndVerifier()
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
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }

      "saman koodin uudelleenkäyttöyritys" - {
        "palauttaa virheen" in {
          val pkce = createChallengeAndVerifier()
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
            val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Attempted authorization code reuse")
          }
        }
        "sulkee koodin" in {
          val pkce = createChallengeAndVerifier()
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
            val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
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
            val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Code not found or it has expired")
          }
        }
        "sulkee access tokenin" in {
          val pkce = createChallengeAndVerifier()
          val code = createAuthorization(validKansalainen, pkce.challenge)

          val token = postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatusOk()
            JsonSerializer.parse[OAuth2AccessTokenSuccessResponse](response.body).access_token
          }

          postAuthorizationServerClientIdFromUsername(
            validPalvelukäyttäjä,
            code = Some(code),
            codeVerifier = Some(pkce.verifier),
            redirectUri = Some(validRedirectUri)
          ) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Attempted authorization code reuse")
          }

          postResourceServer(token) {
            verifyResponseStatus(400)
            val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
            result.error should be("invalid_request")
            result.error_description.get should include("Access token not found or it has expired")
          }
        }
      }
    }

    "code_verifier" - {
      "puuttuminen aiheuttaa virheen" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = None,
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("code_verifier is required")
        }
      }

      "tarkistetaan" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidVerifier = pkce.verifier + "123"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(invalidVerifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Attempted use of invalid code_verifier")
        }
      }

      "väärän verifierin käyttö sulkee koodin" in {
        val pkce = createChallengeAndVerifier()
        val code = createAuthorization(validKansalainen, pkce.challenge)

        val invalidVerifier = pkce.verifier + "123"

        postAuthorizationServerClientIdFromUsername(
          validPalvelukäyttäjä,
          code = Some(code),
          codeVerifier = Some(invalidVerifier),
          redirectUri = Some(validRedirectUri)
        ) {
          verifyResponseStatus(400)
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
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
          val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
          result.error should be("invalid_request")
          result.error_description.get should include("Code not found or it has expired")
        }
      }
    }
  }

  "resource-server rajapinta" - {
    "voi kutsua, kun on käyttöoikeudet" in {
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token) {
        verifyResponseStatusOk()
      }
    }

    "voi kutsua luovutuspalvelu v2 headereilla, kun on käyttöoikeudet" in {
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServerWithLuovutuspalveluV2Headers(token) {
        verifyResponseStatusOk()
      }
    }

    "tekee audit log -merkinnän" in {
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      AuditLogTester.clearMessages()

      postResourceServer(token) {
        verifyResponseStatusOk()

        AuditLogTester.verifyOnlyAuditLogMessage(Map(
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
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token, MockUsers.kalle) {
        verifyResponseStatus(403)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_client")
      }
    }
    "ei voi kutsua, jos liian laaja scope" in {
      // palvelukäyttäjän käyttöoikeudet voivat muuttua myös tokenin elinaikana, siksi tämä on tärkeä testata
      val pkce = createChallengeAndVerifier()
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
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_scope")
        result.error_description.get should include("scope=HENKILOTIEDOT_KAIKKI_TIEDOT exceeds the rights granted to the client oauth2client")
      }
    }
    "ei voi kutsua, kun token on vanhentunut" in {
      val pkce = createChallengeAndVerifier()
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
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_request")
        result.error_description.get should include("Access token not found or it has expired")
      }
    }
    "ei voi kutsua, jos scope on muuttunut epävalidiksi" in {
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      // Vaihda scope epävalidiksi (simuloi taustalla tapahtunutta validointimuutosta)
      runDbSync(
        OAuth2JakoKaikki
          .filter(_.accessTokenSHA256 === sha256(token))
          .map(_.scope)
          .update(
            "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_EI_ENAA_OLEMASSA"
          )
      )

      // Yritä käyttää
      postResourceServer(token) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_scope")
        result.error_description.get should include("scope=HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_EI_ENAA_OLEMASSA contains unknown scopes (OPISKELUOIKEUDET_EI_ENAA_OLEMASSA))")
      }
    }

    "henkilötiedot" - {
      "palautetaan" - {
        "kun vain osa scopeista mukana" in {
          val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

          val pkce = createChallengeAndVerifier()
          val token = createAuthorizationAndToken(validKansalainen, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

          // Tarkista esimerkkidata, että käytetyllä testihenkilöllä on kaikki tiedot olemassa
          validKansalainen.hetu.isDefined should be(true)
          validKansalainen.syntymäaika.isDefined should be(true)

          postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
            verifyResponseStatusOk()
            val data = JsonSerializer.parse[OmaDataOAuth2SuoritetutTutkinnot](response.body)

            data.henkilö.hetu should be(None)
            data.henkilö.oid should be(None)
            data.henkilö.sukunimi should be(Some(validKansalainen.sukunimi))
            data.henkilö.etunimet should be(Some(validKansalainen.etunimet))
            data.henkilö.kutsumanimi should be(Some(validKansalainen.kutsumanimi))
            data.henkilö.syntymäaika should be(validKansalainen.syntymäaika)
          }
        }
        "kun kaikki scopet mukana" in {
          val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU HENKILOTIEDOT_OPPIJANUMERO OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

          val pkce = createChallengeAndVerifier()
          val token = createAuthorizationAndToken(validKansalainen, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

          // Tarkista esimerkkidata, että käytetyllä testihenkilöllä on kaikki tiedot olemassa
          validKansalainen.hetu.isDefined should be(true)
          validKansalainen.syntymäaika.isDefined should be(true)

          postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
            verifyResponseStatusOk()
            val data = JsonSerializer.parse[OmaDataOAuth2SuoritetutTutkinnot](response.body)

            data.henkilö.hetu should be(validKansalainen.hetu)
            data.henkilö.oid should be(Some(validKansalainen.oid))
            data.henkilö.sukunimi should be(Some(validKansalainen.sukunimi))
            data.henkilö.etunimet should be(Some(validKansalainen.etunimet))
            data.henkilö.kutsumanimi should be(Some(validKansalainen.kutsumanimi))
            data.henkilö.syntymäaika should be(validKansalainen.syntymäaika)
          }
        }

        "kun käytetään HENKILOTIEDOT_KAIKKI_TIEDOT ja OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT" in {
          val scope = "HENKILOTIEDOT_KAIKKI_TIEDOT OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

          val pkce = createChallengeAndVerifier()
          val token = createAuthorizationAndToken(validKansalainen, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

          // Tarkista esimerkkidata, että käytetyllä testihenkilöllä on kaikki tiedot olemassa
          validKansalainen.hetu.isDefined should be(true)
          validKansalainen.syntymäaika.isDefined should be(true)

          postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
            verifyResponseStatusOk()
            val data = JsonSerializer.parse[OmaDataOAuth2SuoritetutTutkinnot](response.body)

            data.henkilö.hetu should be(validKansalainen.hetu)
            data.henkilö.oid should be(Some(validKansalainen.oid))
            data.henkilö.sukunimi should be(Some(validKansalainen.sukunimi))
            data.henkilö.etunimet should be(Some(validKansalainen.etunimet))
            data.henkilö.kutsumanimi should be(Some(validKansalainen.kutsumanimi))
            data.henkilö.syntymäaika should be(validKansalainen.syntymäaika)
          }
        }

        "kun käytetään HENKILOTIEDOT_KAIKKI_TIEDOT ja OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT" in {
          val scope = "HENKILOTIEDOT_KAIKKI_TIEDOT OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"

          val pkce = createChallengeAndVerifier()
          val token = createAuthorizationAndToken(validKansalainen, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

          // Tarkista esimerkkidata, että käytetyllä testihenkilöllä on kaikki tiedot olemassa
          validKansalainen.hetu.isDefined should be(true)
          validKansalainen.syntymäaika.isDefined should be(true)

          postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
            verifyResponseStatusOk()
            val data = JsonSerializer.parse[OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet](response.body)

            data.henkilö.hetu should be(validKansalainen.hetu)
            data.henkilö.oid should be(Some(validKansalainen.oid))
            data.henkilö.sukunimi should be(Some(validKansalainen.sukunimi))
            data.henkilö.etunimet should be(Some(validKansalainen.etunimet))
            data.henkilö.kutsumanimi should be(Some(validKansalainen.kutsumanimi))
            data.henkilö.syntymäaika should be(validKansalainen.syntymäaika)
          }
        }

        "kun käytetään HENKILOTIEDOT_KAIKKI_TIEDOT ja OPISKELUOIKEUDET_KAIKKI_TIEDOT" in {
          val scope = "HENKILOTIEDOT_KAIKKI_TIEDOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

          val pkce = createChallengeAndVerifier()
          val token = createAuthorizationAndToken(validKansalainen, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

          // Tarkista esimerkkidata, että käytetyllä testihenkilöllä on kaikki tiedot olemassa
          validKansalainen.hetu.isDefined should be(true)
          validKansalainen.syntymäaika.isDefined should be(true)

          postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
            verifyResponseStatusOk()
            val data = JsonSerializer.parse[OmaDataOAuth2KaikkiOpiskeluoikeudet](response.body)

            data.henkilö.hetu should be(validKansalainen.hetu)
            data.henkilö.oid should be(Some(validKansalainen.oid))
            data.henkilö.sukunimi should be(Some(validKansalainen.sukunimi))
            data.henkilö.etunimet should be(Some(validKansalainen.etunimet))
            data.henkilö.kutsumanimi should be(Some(validKansalainen.kutsumanimi))
            data.henkilö.syntymäaika should be(validKansalainen.syntymäaika)
          }
        }
      }
    }

    "suoritetut tutkinnot" - {
      "palautetaan" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen

        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
        val expectedSuoritusData = expectedOoData.suoritukset.head

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()
          val data = JsonSerializer.parse[OmaDataOAuth2SuoritetutTutkinnot](response.body)

          data.opiskeluoikeudet should have length 1
          data.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotAmmatillinenOpiskeluoikeus]

          val actualOo = data.opiskeluoikeudet.head
          val actualSuoritus = actualOo.suoritukset.head

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
        }
      }
      "audit-logitetaan" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        AuditLogTester.clearMessages()

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()

          AuditLogTester.verifyOnlyAuditLogMessage(Map(
            "operation" -> "OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT",
            "target" -> Map(
              "oppijaHenkiloOid" -> oppija.oid,
              "omaDataKumppani" -> MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä.username,
              "omaDataOAuth2Scope" -> scope
            ),
          ))
        }
      }
    }
    "aktiiviset opinnot" - {
      "palautetaan" in {
        val oppija = KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty

        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo)
        val expectedSuoritusData = expectedOoData.suoritukset.head

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()
          val data = JsonSerializer.parse[OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet](response.body)

          data.opiskeluoikeudet should have length 1
          data.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus]

          val actualOo = data.opiskeluoikeudet.head
          val actualSuoritus = actualOo.suoritukset.head

          verifyOpiskeluoikeusJaSuoritus(actualOo, Seq(actualSuoritus), expectedOoData, Seq(expectedSuoritusData))
        }
      }
      "audit-logitetaan" in {
        val oppija = KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        AuditLogTester.clearMessages()

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()

          AuditLogTester.verifyOnlyAuditLogMessage(Map(
            "operation" -> "OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT",
            "target" -> Map(
              "oppijaHenkiloOid" -> oppija.oid,
              "omaDataKumppani" -> MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä.username,
              "omaDataOAuth2Scope" -> scope
            ),
          ))
        }
      }
    }
    "kaikki opinnot" - {
      "palautetaan" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen

        val expectedOoData = getOpiskeluoikeudet(oppija.oid, MockUsers.viranomainen)
          .find(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
          .get

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_KAIKKI_TIEDOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()
          val data = JsonSerializer.parse[OmaDataOAuth2KaikkiOpiskeluoikeudet](response.body)

          data.opiskeluoikeudet should have length 1
          data.opiskeluoikeudet.head shouldBe a[AmmatillinenOpiskeluoikeus]

          val actualOo = data.opiskeluoikeudet.head.asInstanceOf[AmmatillinenOpiskeluoikeus]

          actualOo should be(expectedOoData)
        }
      }

      "Ei palauteta luottamuksellisiksi määriteltyjä kenttiä" in {
        val oppija = KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija

        val expectedOoData = getOpiskeluoikeudet(oppija.oid, MockUsers.viranomainen)
          .find(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
          .get

        // Varmista, että oppijalla on luottamuksellisia kenttiä:
        val fullOoData = getOpiskeluoikeudet(oppija.oid, MockUsers.kalle)
          .find(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
          .get
        expectedOoData.lisätiedot.map(_.asInstanceOf[PerusopetuksenOpiskeluoikeudenLisätiedot]).flatMap(_.vuosiluokkiinSitoutumatonOpetus) should be(None)
        fullOoData.lisätiedot.map(_.asInstanceOf[PerusopetuksenOpiskeluoikeudenLisätiedot]).flatMap(_.vuosiluokkiinSitoutumatonOpetus) should be(Some(true))

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_KAIKKI_TIEDOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()
          val data = JsonSerializer.parse[OmaDataOAuth2KaikkiOpiskeluoikeudet](response.body)

          data.opiskeluoikeudet should have length 1
          data.opiskeluoikeudet.head shouldBe a[PerusopetuksenOpiskeluoikeus]

          val actualOo = data.opiskeluoikeudet.head.asInstanceOf[PerusopetuksenOpiskeluoikeus]

          actualOo should be(expectedOoData)
        }
      }

      "audit-logitetaan" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen

        val scope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_KAIKKI_TIEDOT"

        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, scope, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä)

        AuditLogTester.clearMessages()

        postResourceServer(token, MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä) {
          verifyResponseStatusOk()

          AuditLogTester.verifyOnlyAuditLogMessage(Map(
            "operation" -> "OAUTH2_KATSOMINEN_KAIKKI_TIEDOT",
            "target" -> Map(
              "oppijaHenkiloOid" -> oppija.oid,
              "omaDataKumppani" -> MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä.username,
              "omaDataOAuth2Scope" -> scope
            ),
          ))

        }
      }
    }

    val olemassaolematonOppijaOid = FixtureCreator.generateOppijaOid(999999978)

    Seq("OPISKELUOIKEUDET_KAIKKI_TIEDOT", "OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT", "OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT").map(ooScope => {
      s"Kun oppijan kaikki opiskeluoikeudet on mitätöity, palautetaan 404 - ${ooScope}" in {
        val mitätöityOppijaOid = KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid
        val palveluKäyttäjä = MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä
        val clientId = palveluKäyttäjä.username

        val scope = s"HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI ${ooScope}"

        val pkce = createChallengeAndVerifier()

        KoskiApplicationForTests.omaDataOAuth2Repository.create(
          code = s"foo${ooScope}",
          oppijaOid = mitätöityOppijaOid,
          clientId,
          scope,
          codeChallenge = pkce.challenge,
          redirectUri = validRedirectUri
        ).isRight should be(true)

        val token = KoskiApplicationForTests.omaDataOAuth2Repository.createAccessTokenForCode(
            s"foo${ooScope}",
            clientId,
            pkce.challenge,
            Some(validRedirectUri),
            scope.split(" ").toSet
          ).getOrElse(throw new Error("Internal error"))
          .accessToken

        postResourceServer(token, palveluKäyttäjä) {
          verifyResponseStatus(404)
        }
      }

      s"kun oppijaa ei löydy lainkaan, palautetaan 404 - ${ooScope}" in {
        val palveluKäyttäjä = MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä
        val clientId = palveluKäyttäjä.username

        val scope = s"HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI ${ooScope}"

        val pkce = createChallengeAndVerifier()

        KoskiApplicationForTests.omaDataOAuth2Repository.create(
          code = s"foobar${ooScope}",
          oppijaOid = olemassaolematonOppijaOid,
          clientId,
          scope,
          codeChallenge = pkce.challenge,
          redirectUri = validRedirectUri
        ).isRight should be(true)

        val token = KoskiApplicationForTests.omaDataOAuth2Repository.createAccessTokenForCode(
            s"foobar${ooScope}",
            clientId,
            pkce.challenge,
            Some(validRedirectUri),
            scope.split(" ").toSet
          ).getOrElse(throw new Error("Internal error"))
          .accessToken

        postResourceServer(token, palveluKäyttäjä) {
          verifyResponseStatus(404)
        }
      }
    })
  }

  "suostumusten hallinta" - {
    "kirjautumaton palauttaa 401" in {
      get(uri = "api/omadata-oauth2/resource-owner/active-consents", params = Nil) {
        verifyResponseStatus(401)
      }
    }

    "palauttaa kansalaisen antaman suostumuksen" in {
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus = PerusopetusExampleData.päättötodistusOpiskeluoikeus(), henkilö = validKansalainen) {
        verifyResponseStatusOk()
      }
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token) {
        verifyResponseStatusOk()
      }

      get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
        items.head.clientId should be(validClientId)
      }
    }

    "suostumuksen voi perua, ja se aiheuttaa audit lokimerkinnän" in {
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus = PerusopetusExampleData.päättötodistusOpiskeluoikeus(), henkilö = validKansalainen) {
        verifyResponseStatusOk()
      }
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token) {
        verifyResponseStatusOk()
      }

      val code = get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
        items.head.codeSHA256
      }

      delete(uri = "api/omadata-oauth2/resource-owner/active-consents/" + code, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "KANSALAINEN_MYDATA_POISTO",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "omaDataKumppani" -> validClientId,
            "omaDataOAuth2Scope" -> validScope
          ),
        ))
      }

      get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items should be(empty)
      }
    }

    "toinen oppija ei voi perua toisen suostumusta" in {
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus = PerusopetusExampleData.päättötodistusOpiskeluoikeus(), henkilö = validKansalainen) {
        verifyResponseStatusOk()
      }
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce)

      postResourceServer(token) {
        verifyResponseStatusOk()
      }

      val code = get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
        items.head.codeSHA256
      }

      delete(uri = "api/omadata-oauth2/resource-owner/active-consents/" + code, headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.markkanen.hetu.get)) {
        verifyResponseStatus(400)
      }

      get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
      }
    }

    "master oidilla voi hakea slave oidilla tallennettua suostumusta" in {

      clearOppijanOpiskeluoikeudet(KoskiSpecificMockOppijat.master.oid)
      clearOppijanOpiskeluoikeudet(KoskiSpecificMockOppijat.slave.henkilö.oid)

      KoskiApplicationForTests.omaDataOAuth2Repository.create(
        "asd",
        KoskiSpecificMockOppijat.slave.henkilö.oid,
        validClientId,
        validScope,
        createValidDummyCodeChallenge,
        validRedirectUri
      ).isRight should be(true)

      get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.master.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
      }
    }


    "master oidilla voi perua slave oidilla tallennettua suostumusta" in {

      clearOppijanOpiskeluoikeudet(KoskiSpecificMockOppijat.master.oid)
      clearOppijanOpiskeluoikeudet(KoskiSpecificMockOppijat.slave.henkilö.oid)

      KoskiApplicationForTests.omaDataOAuth2Repository.create(
        "asd",
        KoskiSpecificMockOppijat.slave.henkilö.oid,
        validClientId,
        validScope,
        createValidDummyCodeChallenge,
        validRedirectUri
      ).isRight should be(true)

      val code = get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.master.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items.length should be(1)
        items.head.codeSHA256
      }

      delete(uri = "api/omadata-oauth2/resource-owner/active-consents/" + code, headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.master.hetu.get)) {
        verifyResponseStatusOk()
      }

      get(uri = "api/omadata-oauth2/resource-owner/active-consents", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.master.hetu.get)) {
        verifyResponseStatusOk()
        val items = JsonSerializer.parse[List[OmaDataOAuth2JakoItem]](response.body)
        items should be(empty)
      }
    }
  }

  private def postResourceServer[T](token: String, user: KoskiMockUser = validPalvelukäyttäjä)(f: => T): T = {
    val tokenHeaders = Map("X-Auth" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = certificateHeaders(user) ++ tokenHeaders)(f)
  }

  private def postResourceServerWithLuovutuspalveluV2Headers[T](token: String)(f: => T): T = {
    val headers = Map(
      "Authorization" -> s"Bearer ${token}",
      "x-amzn-mtls-clientcert-subject" -> "CN=oauth2client",
      "x-amzn-mtls-clientcert-serial-number" -> "123",
      "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
      "X-Forwarded-For" -> "0.0.0.0"
    )
    post(uri = "api/omadata-oauth2/resource-server", headers = headers)(f)
  }
}

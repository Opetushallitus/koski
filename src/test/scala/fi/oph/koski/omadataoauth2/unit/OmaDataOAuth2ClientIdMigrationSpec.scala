package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.DatabaseTestMethods
import fi.oph.koski.db.KoskiTables.OAuth2JakoKaikki
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.{createChallengeAndVerifier, sha256}
import fi.oph.koski.omadataoauth2.{OAuth2AccessTokenSuccessResponse, OAuth2ErrorResponse}

class OmaDataOAuth2ClientIdMigrationSpec extends OmaDataOAuth2TestBase with DatabaseTestMethods {

  private val jaettuSubjectDn = "CN=oauth2vaihto"

  private val vanhaKäyttäjä = MockUsers.omadataOAuth2VaihtuvaVanhaPalvelukäyttäjä
  private val uusiKäyttäjä = MockUsers.omadataOAuth2VaihtuvaUusiPalvelukäyttäjä
  private val ilmanOikeuksiaKäyttäjä = MockUsers.omadataOAuth2VaihtuvaIlmanOikeuksiaPalvelukäyttäjä

  "authorization-server rajapinta" - {
    "vanha client_id toimii jaetulla varmenteella" in {
      hankiToken(vanhaKäyttäjä) should not be empty
    }

    "uusi client_id toimii jaetulla varmenteella" in {
      hankiToken(uusiKäyttäjä) should not be empty
    }

    "client_id, jota ei ole konfiguroitu tälle varmenteelle, hylätään" in {
      val pkce = createChallengeAndVerifier()
      val code = createAuthorization(validKansalainen, pkce.challenge, user = validPalvelukäyttäjä)

      postAuthorizationServerJaetullaVarmenteella(validPalvelukäyttäjä.username, code, pkce.verifier) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_client")
        result.error_description.get should include("ei vastaa mTLS-varmenteelle konfiguroitua käyttäjätunnusta")
      }
    }

    "varmenteelle konfiguroitu client_id ilman OmaData OAuth2 -käyttöoikeuksia hylätään" in {
      val pkce = createChallengeAndVerifier()
      val code = createAuthorization(validKansalainen, pkce.challenge, user = ilmanOikeuksiaKäyttäjä)

      postAuthorizationServerJaetullaVarmenteella(ilmanOikeuksiaKäyttäjä.username, code, pkce.verifier) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_client")
        result.error_description.get should include("ei vastaa mTLS-varmenteelle konfiguroitua käyttäjätunnusta")
      }
    }
  }

  "resource-server rajapinta" - {
    "vanhalle client_id:lle myönnetty token toimii ja kirjautuu audit lokiin vanhalla tunnuksella" in {
      val token = hankiToken(vanhaKäyttäjä)

      AuditLogTester.clearMessages()

      postResourceServerJaetullaVarmenteella(token) {
        verifyResponseStatusOk()

        AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
          "operation" -> "OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "omaDataKumppani" -> vanhaKäyttäjä.username,
            "omaDataOAuth2Scope" -> validScope
          ),
        ))
      }
    }

    "uudelle client_id:lle myönnetty token toimii ja kirjautuu audit lokiin uudella tunnuksella" in {
      val token = hankiToken(uusiKäyttäjä)

      AuditLogTester.clearMessages()

      postResourceServerJaetullaVarmenteella(token) {
        verifyResponseStatusOk()

        AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
          "operation" -> "OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "omaDataKumppani" -> uusiKäyttäjä.username,
            "omaDataOAuth2Scope" -> validScope
          ),
        ))
      }
    }

    "scope tarkistetaan sen tunnuksen oikeuksia vasten, jolle token myönnettiin" in {
      val token = hankiToken(vanhaKäyttäjä)

      // HENKILOTIEDOT_HETU sisältyy uuden mutta ei vanhan tunnuksen käyttöoikeuksiin
      runDbSync(
        OAuth2JakoKaikki
          .filter(_.accessTokenSHA256 === sha256(token))
          .map(_.scope)
          .update(validScope + " HENKILOTIEDOT_HETU")
      )

      postResourceServerJaetullaVarmenteella(token) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error should be("invalid_scope")
      }
    }

    "token, jonka client_id:tä ei ole konfiguroitu tälle varmenteelle, ei kelpaa" in {
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(validKansalainen, pkce, user = validPalvelukäyttäjä)

      postResourceServerJaetullaVarmenteella(token) {
        verifyResponseStatus(400)
        val result = JsonSerializer.parse[OAuth2ErrorResponse](response.body)
        result.error_description.get should include("Access token not found or it has expired")
      }
    }
  }

  private def hankiToken(user: KoskiMockUser): String = {
    val pkce = createChallengeAndVerifier()
    val code = createAuthorization(validKansalainen, pkce.challenge, user = user)

    postAuthorizationServerJaetullaVarmenteella(user.username, code, pkce.verifier) {
      verifyResponseStatusOk()
      JsonSerializer.parse[OAuth2AccessTokenSuccessResponse](response.body).access_token
    }
  }

  private def postAuthorizationServerJaetullaVarmenteella[T](clientId: String, code: String, codeVerifier: String)(f: => T): T =
    post(
      uri = "api/omadata-oauth2/authorization-server",
      body = createFormParametersBody(
        grantType = Some("authorization_code"),
        code = Some(code),
        codeVerifier = Some(codeVerifier),
        clientId = Some(clientId),
        redirectUri = Some(validRedirectUri)
      ),
      headers = certificateHeaders(jaettuSubjectDn) ++ formContent
    )(f)

  private def postResourceServerJaetullaVarmenteella[T](token: String)(f: => T): T =
    post(
      uri = "api/omadata-oauth2/resource-server",
      headers = certificateHeaders(jaettuSubjectDn) ++ Map("Authorization" -> s"Bearer ${token}")
    )(f)
}

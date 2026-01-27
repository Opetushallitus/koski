package fi.oph.koski.hsl

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.createChallengeAndVerifier
import fi.oph.koski.omadataoauth2.unit.OmaDataOAuth2TestBase
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.json4s.{JNothing, JObject}

class HslOmaDataOAuth2Spec extends OmaDataOAuth2TestBase with OpiskeluoikeusTestMethods {

  val hslUser = MockUsers.hslOmaDataOAuth2Palvelukäyttäjä
  val hslClientId = hslUser.username

  val hslScope = "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_OPPIJANUMERO OPISKELUOIKEUDET_HSL"

  override def createValidAuthorizeParams: Seq[(String, String)] = Seq(
    ("client_id", hslClientId),
    ("response_type", "code"),
    ("response_mode", "form_post"),
    ("redirect_uri", validRedirectUri),
    ("code_challenge", createValidDummyCodeChallenge),
    ("code_challenge_method", "S256"),
    ("state", validState),
    ("scope", hslScope)
  )

  "HSL OAuth2 resource-server rajapinta" - {
    "voi kutsua HSL-scopella kun on käyttöoikeudet" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

      postResourceServer(token, hslUser) {
        verifyResponseStatusOk()
      }
    }

    "tekee audit-lokimerkinnän OAUTH2_KATSOMINEN_HSL" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

      AuditLogTester.clearMessages()

      postResourceServer(token, hslUser) {
        verifyResponseStatusOk()

        AuditLogTester.verifyOnlyAuditLogMessage(Map(
          "operation" -> "OAUTH2_KATSOMINEN_HSL",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppija.oid,
            "omaDataKumppani" -> hslClientId,
            "omaDataOAuth2Scope" -> hslScope
          ),
        ))
      }
    }

    "palauttaa oppijan tiedot" - {
      "henkilötiedoissa vain oid ja syntymäaika" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

        postResourceServer(token, hslUser) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(response.body)
          val henkilö = json \ "henkilö"

          (henkilö \ "oid").extract[String] should equal(oppija.oid)
          oppija.syntymäaika.foreach { syntymäaika =>
            (henkilö \ "syntymäaika").extract[String] should equal(syntymäaika.toString)
          }
          (henkilö \ "hetu") should equal(JNothing)
          (henkilö \ "sukunimi") should equal(JNothing)
          (henkilö \ "etunimet") should equal(JNothing)
        }
      }

      "opiskeluoikeudet sisältävät tilan ja oppilaitoksen" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

        postResourceServer(token, hslUser) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(response.body)
          val opiskeluoikeudet = (json \ "opiskeluoikeudet").children

          opiskeluoikeudet should not be empty

          val firstOo = opiskeluoikeudet.head
          (firstOo \ "tyyppi" \ "koodiarvo").extract[String] should not be empty
          (firstOo \ "tila" \ "opiskeluoikeusjaksot") should not equal JNothing
          (firstOo \ "oppilaitos") should not equal JNothing
        }
      }

      "tokenInfo sisältää scopen ja voimassaoloajan" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

        postResourceServer(token, hslUser) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(response.body)
          val tokenInfo = json \ "tokenInfo"

          (tokenInfo \ "scope").extract[String] should equal(hslScope)
          (tokenInfo \ "expirationTime").extract[String] should not be empty
        }
      }
    }

    "suodattaa vain HSL-skeemassa tuetut opiskeluoikeustyypit" in {
      val oppija = KoskiSpecificMockOppijat.eero
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

      postResourceServer(token, hslUser) {
        verifyResponseStatusOk()
        val json = JsonMethods.parse(response.body)
        val opiskeluoikeudet = (json \ "opiskeluoikeudet").children

        val tyypit = opiskeluoikeudet.map(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String])
        val tuetutTyypit = Set(
          "ammatillinenkoulutus",
          "aikuistenperusopetus",
          "diatutkinto",
          "ebtutkinto",
          "europeanschoolofhelsinki",
          "ibtutkinto",
          "internationalschool",
          "korkeakoulutus",
          "lukiokoulutus",
          "perusopetukseenvalmistavaopetus",
          "perusopetus",
          "tuva",
          "ylioppilastutkinto"
        )

        tyypit.foreach { tyyppi =>
          tuetutTyypit should contain(tyyppi)
        }
      }
    }

    "ei voi kutsua ilman käyttöoikeuksia" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen
      val pkce = createChallengeAndVerifier()
      val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

      postResourceServer(token, MockUsers.kalle) {
        verifyResponseStatus(403)
      }
    }

    "palauttaa 404 jos oppijalla ei ole opiskeluoikeuksia" in {
      val oppija = KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia
      val pkce = createChallengeAndVerifier()

      val code = createAuthorization(oppija, pkce.challenge, hslScope, hslUser)
      val token = postAuthorizationServer(
        hslUser,
        clientId = Some(hslClientId),
        code = Some(code),
        codeVerifier = Some(pkce.verifier),
        redirectUri = Some(validRedirectUri)
      ) {
        verifyResponseStatusOk()
        JsonSerializer.parse[fi.oph.koski.omadataoauth2.OAuth2AccessTokenSuccessResponse](response.body).access_token
      }

      postResourceServer(token, hslUser) {
        verifyResponseStatus(404)
      }
    }

    "ammatillisen opiskeluoikeuden erityistiedot" - {
      "sisältää tiedon oppisopimuksesta" in {
        val oppija = KoskiSpecificMockOppijat.reformitutkinto
        val pkce = createChallengeAndVerifier()
        val token = createAuthorizationAndToken(oppija, pkce, hslScope, hslUser)

        postResourceServer(token, hslUser) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(response.body)
          val opiskeluoikeudet = (json \ "opiskeluoikeudet").children

          val ammatilliset = opiskeluoikeudet.filter { oo =>
            (oo \ "tyyppi" \ "koodiarvo").extract[String] == "ammatillinenkoulutus"
          }

          ammatilliset should not be empty

          val suoritukset = (ammatilliset.head \ "suoritukset").children
          suoritukset should not be empty

          val osaamisenHankkimistavat = (suoritukset.head \ "osaamisenHankkimistavat").children
          val oppisopimuksellinen = osaamisenHankkimistavat.find { oh =>
            (oh \ "osaamisenHankkimistapa" \ "tunniste" \ "koodiarvo").extractOpt[String].contains("oppisopimus")
          }

          oppisopimuksellinen should not be empty
        }
      }
    }
  }

  private def postResourceServer[T](token: String, user: fi.oph.koski.koskiuser.KoskiMockUser = hslUser)(f: => T): T = {
    val tokenHeaders = Map("Authorization" -> s"Bearer ${token}")
    post(uri = "api/omadata-oauth2/resource-server", headers = certificateHeaders(user) ++ tokenHeaders)(f)
  }
}

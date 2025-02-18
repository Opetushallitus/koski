package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.{OmaDataOAuth2Error, OmaDataOAuth2ErrorType}
import org.http4s.Uri

class OmaDataOAuth2FrontendSpec extends OmaDataOAuth2TestBase {
  val app = KoskiApplicationForTests

  val tuntematonClientId = "loytymatonClientId"
  val vääräRedirectUri = "/koski/omadata-oauth2/EI-OLE/debug-post-response"

  "resource-owner authorize frontend -rajapinta" - {
    "toimivilla parametreilla" - {
      "kirjautumattomalla käyttäjällä" - {
        "redirectaa login-sivulle, joka redirectaa sivulle, jossa parametrit base64url-enkoodattuna" in {

          val authorizeParamsString = createValidAuthorizeParamsString

          val serverUri = s"${authorizeFrontendBaseUri}?${authorizeParamsString}"
          val expectedLoginUri = s"/koski/login/oppija?locale=fi&service=/koski/user/login?onSuccess=/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(authorizeParamsString)}"

          get(
            uri = serverUri
          ) {
            verifyResponseStatus(302)

            response.header("Location") should include(expectedLoginUri)
          }
        }
      }
      "kirjautuneella käyttäjällä" - {
        "palauttaa suostumuksen myöntämis -sivun" in {
          val serverUri = s"${authorizeFrontendBaseUri}?${createValidAuthorizeParamsString}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatusOk()
            body should include("/koski/js/koski-omadataoauth2.js")
          }
        }
      }
    }

    "scopella, joka ei annetulle clientille sallittu vaikka muuten validi" - {
      "kirjautumattomalla käyttäjällä" - {
        "redirectaa login-sivulle" in {
          // Tämä testi vain varmistaa, että palvelukäyttäjätunnukseen(=client_id) sidottu tarkka sallittu scope ei paljastu ulos tässä vaiheessa
          val parametriNimi = "scope"
          val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"

          val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

          val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

          val expectedLoginUri = s"/koski/login/oppija?locale=fi&service=/koski/user/login?onSuccess=/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(eiSallitullaScopella)}"

          get(
            uri = serverUri
          ) {
            verifyResponseStatus(302)

            response.header("Location") should include(expectedLoginUri)
          }
        }
      }
      "kirjautuneella käyttäjällä" - {
        "palauttaa suostumuksen myöntämis -sivun" in {
          // Tämä testi vain varmistaa, että palvelukäyttäjätunnukseen(=client_id) sidottu tarkka sallittu scope ei paljastu ulos tässä vaiheessa
          val parametriNimi = "scope"
          val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"

          val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

          val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatusOk()
            body should include("/koski/js/koski-omadataoauth2.js")          }
        }
      }
    }

    "viallisella client_id/redirect_uri:lla" - {
      "redirectaa käyttäjän samaan osoitteeseen query-parametreihin sisällytetyllä virheilmoituksella" - {
        Seq("client_id", "redirect_uri").foreach(paramName => {
          s"kun ${paramName} puuttuu" - {
            "kirjautuneella käyttäjällä" in {
              val väärälläParametrilla = createParamsString(createValidAuthorizeParams.filterNot(_._1 == paramName))
              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex (expectedErrorMessageRegexp)
              }
            }
            "kirjautumattomalla käyttäjällä" in {
              val väärälläParametrilla = createParamsString(createValidAuthorizeParams.filterNot(_._1 == paramName))
              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex (expectedErrorMessageRegexp)
              }
            }
          }
        })

        Seq("client_id", "redirect_uri").foreach(paramName => {
          s"kun ${paramName} on annettu useammin kuin kerran" - {
            "kirjautuneella käyttäjällä" in {
              val validValue = createValidAuthorizeParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(createValidAuthorizeParams :+ (paramName, validValue))

              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex (expectedErrorMessageRegexp)
              }
            }
          }
        })

        Seq("client_id", "redirect_uri", "state").foreach(paramName => {
          s"kun ${paramName} on annettu useammin kuin kerran" - {
            "kirjautumattomalla käyttäjällä" in {
              val validValue = createValidAuthorizeParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(createValidAuthorizeParams :+ (paramName, validValue))

              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex (expectedErrorMessageRegexp)
              }
            }
          }
        })

        "kun client_id on tuntematon" - {
          "kirjautuneella käyttäjällä" in {
            val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex (expectedErrorMessageRegexp)
            }
          }
          "kirjautumattomalla käyttäjällä" in {
            val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex (expectedErrorMessageRegexp)
            }
          }
        }

        "kun redirect_uri ei ole annetun client_idn tallennettu redirect_uri" - {
          "kirjautuneella käyttäjällä" in {
            val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex (expectedErrorMessageRegexp)
            }
          }
          "kirjautumattomalla käyttäjällä" in {
            val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex (expectedErrorMessageRegexp)
            }
          }
        }
      }
    }

    "valideilla client_id/redirect_uri:lla" - {
      val muutPakollisetParametrinimet = Seq(
        "response_type",
        "response_mode",
        "code_challenge",
        "code_challenge_method",
        "scope"
      )

      muutPakollisetParametrinimet.foreach(paramName => {
        s"kun pakollinen parametri ${paramName} puuttuu kokonaan" - {
          "redirectaa virhetiedot palauttavalle post response -sivulle" - {
            "kirjautumattomana" in {
              val puuttuvallaParametrilla = createParamsString(validParamsIlman(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${puuttuvallaParametrilla}"

              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should not include (s"logout")
                response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
                response.header("Location") should include(s"error=invalid_request")
                response.header("Location") should include(queryStringUrlEncode(s"required parameter ${paramName} missing"))
                response.header("Location") should include regex ("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
              }
            }
            "kirjautuneena clientilla, jolle ei haluta logouttia" in {
              val puuttuvallaParametrilla = createParamsString(vaihdetullaArvolla(validParamsIlman(paramName), "client_id", validClientIdEiLogouttia))

              val serverUri = s"${authorizeFrontendBaseUri}?${puuttuvallaParametrilla}"

              val expectedError = "invalid_request"
              val expectedParams =
                Seq(
                  ("client_id", validClientIdEiLogouttia),
                  ("redirect_uri", validRedirectUri),
                  ("state", validState),
                  ("error", expectedError)
                )

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
                val actualParams = Uri.unsafeFromString(response.header("Location")).params

                actualParams should contain allElementsOf (expectedParams)
                actualParams("error_description") should include(s"required parameter ${paramName} missing")
              }
            }
          }
          "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
            "kirjautuneena clientilla, jolle halutaan logout" in {
              val puuttuvallaParametrilla = createParamsString(validParamsIlman(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${puuttuvallaParametrilla}"

              val expectedError = "invalid_request"
              val expectedParams =
                Seq(
                  ("client_id", validClientId),
                  ("redirect_uri", validRedirectUri),
                  ("state", validState),
                  ("error", expectedError)
                )

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
                val base64UrlEncodedParams = response.header("Location").split("/").last
                encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
                encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"required parameter ${paramName} missing")
              }
            }
          }
        }
      })

      muutPakollisetParametrinimet.foreach(paramName => {
        s"kun parametri ${paramName} esiintyy useamman kerran" - {
          "redirectaa virhetiedot palauttavalle post response -sivulle" - {
            "kirjautumattomana" in {
              val duplikaattiParametrilla = createParamsString(validParamsDuplikaatilla(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${duplikaattiParametrilla}"

              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should not include(s"logout")
                response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
                response.header("Location") should include(s"error=invalid_request")
                response.header("Location") should include(queryStringUrlEncode(s"parameter ${paramName} is repeated"))
                response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
              }
            }
            "kirjautuneena clientilla, jolle ei haluta logouttia" in {
              val duplikaattiParametrilla = createParamsString(duplikaatilla(validParamsVaihdetullaArvolla("client_id", validClientIdEiLogouttia), paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${duplikaattiParametrilla}"

              val expectedError = "invalid_request"
              val expectedParams =
                Seq(
                  ("client_id", validClientIdEiLogouttia),
                  ("redirect_uri", validRedirectUri),
                  ("state", validState),
                  ("error", expectedError)
                )

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
                val actualParams = Uri.unsafeFromString(response.header("Location")).params
                actualParams should contain allElementsOf (expectedParams)
                actualParams("error_description") should include(s"parameter ${paramName} is repeated")
              }
            }
          }
          "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
            "kirjautuneena clientilla, jolle halutaan logout" in {
              val duplikaattiParametrilla = createParamsString(validParamsDuplikaatilla(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${duplikaattiParametrilla}"

              val expectedError = "invalid_request"
              val expectedParams =
                Seq(
                  ("client_id", validClientId),
                  ("redirect_uri", validRedirectUri),
                  ("state", validState),
                  ("error", expectedError)
                )

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
                val base64UrlEncodedParams = response.header("Location").split("/").last
                encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
                encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"parameter ${paramName} is repeated")
              }
            }
          }
        }
      })

      "kun response_type ei ole code" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "response_type"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "code"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_request")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "response_type"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "code"

            val väärälläParametrilla = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "response_type"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "code"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
      }

      "kun response_mode ei ole form_post" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "response_mode"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "form_post"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_request")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "response_mode"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "form_post"

            val väärälläParametrilla = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "response_mode"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "form_post"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
      }

      "kun code_challenge_method ei ole S256" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "code_challenge_method"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "S256"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_request")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "code_challenge_method"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "S256"

            val väärälläParametrilla = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "code_challenge_method"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "S256"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedError = "invalid_request"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${tuntematonArvo} not supported. Supported values: ${tuetutArvot}")
            }
          }
        }
      }

      "kun scope on epävalidi" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include (s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} contains unknown scopes (HENKILOTIEDOT_VIRHEELLINEN)"))
              response.header("Location") should include regex ("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {

            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

            val eiSallitullaScopella = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${eiSallittuArvo} contains unknown scopes (HENKILOTIEDOT_VIRHEELLINEN))")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${eiSallittuArvo} contains unknown scopes (HENKILOTIEDOT_VIRHEELLINEN))")
            }
          }
        }
      }

      "kun scopessa on toistensa kanssa epäyhteensopivia arvoja" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include (s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} contains an invalid combination of scopes (OPISKELUOIKEUDET_KAIKKI_TIEDOT, OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT)"))
              response.header("Location") should include regex ("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${eiSallittuArvo} contains an invalid combination of scopes (OPISKELUOIKEUDET_KAIKKI_TIEDOT, OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT))")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${eiSallittuArvo} contains an invalid combination of scopes (OPISKELUOIKEUDET_KAIKKI_TIEDOT, OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT))")
            }
          }
        }
      }

      "kun scopesta puuttuu opiskeluoikeudet-scope kokonaan" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include (s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} is missing a required OPISKELUOIKEUDET_ scope"))
              response.header("Location") should include regex ("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU"

            val eiSallitullaScopella = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${eiSallittuArvo} is missing a required OPISKELUOIKEUDET_ scope")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${eiSallittuArvo} is missing a required OPISKELUOIKEUDET_ scope")
            }
          }
        }
      }

      "kun scopesta puuttuu henkilötiedot-scope kokonaan" - {
        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          "kirjautumattomana" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} is missing a required HENKILOTIEDOT_ scope"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kirjautuneena clientilla, jolle ei haluta logouttia" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(vaihdetullaArvolla(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo), "client_id", validClientIdEiLogouttia))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientIdEiLogouttia),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              val actualParams = Uri.unsafeFromString(response.header("Location")).params
              actualParams should contain allElementsOf (expectedParams)
              actualParams("error_description") should include(s"${parametriNimi}=${eiSallittuArvo} is missing a required HENKILOTIEDOT_ scope")
            }
          }
        }
        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {
          "kirjautuneena clientilla, jolle halutaan logout" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            val expectedError = "invalid_scope"
            val expectedParams =
              Seq(
                ("client_id", validClientId),
                ("redirect_uri", validRedirectUri),
                ("state", validState),
                ("error", expectedError)
              )

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              val base64UrlEncodedParams = response.header("Location").split("/").last
              encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
              encodedParamStringShouldContainErrorDescriptionWithUuid(base64UrlEncodedParams, s"${parametriNimi}=${eiSallittuArvo} is missing a required HENKILOTIEDOT_ scope")
            }
          }
        }
      }
    }
  }

  "resource-owner authorize -rajapinta" - {
    "ei toimi ilman kirjautumista" in {
      val serverUri = s"${authorizeBaseUri}?${createValidAuthorizeParamsString}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(401)
      }
    }

    "logouttaavalla clientilla" - {
      "palauttaa authorization code:n" in {
        val serverUri = s"${authorizeBaseUri}?${createValidAuthorizeParamsString}"

        val expectedResultParams =
          Seq(
            ("client_id", validClientId),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
          )

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
          val base64UrlEncodedParams = response.header("Location").split("/").last

          encodedParamStringShouldContain(base64UrlEncodedParams, expectedResultParams)

          val code = getFromEncodedParamString(base64UrlEncodedParams, "code")
          code.isDefined should be(true)
        }
      }

      "tekee auditlokituksen" in {
        AuditLogTester.clearMessages()

        val serverUri = s"${authorizeBaseUri}?${createValidAuthorizeParamsString}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> "KANSALAINEN_MYDATA_LISAYS",
            "target" -> Map(
              "oppijaHenkiloOid" -> oppijaOid,
              "omaDataKumppani" -> validClientId,
              "omaDataOAuth2Scope" -> validScope
            ),
          ))
        }
      }

      "ei salli saman code_challenge:n käyttämistä monta kertaa" in {
        val paramsString = createValidAuthorizeParamsString
        val serverUri = s"${authorizeBaseUri}?${paramsString}"
        val expectedSuccessResultParams =
          Seq(
            ("client_id", validClientId),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
          )

        get(uri = serverUri, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
          val base64UrlEncodedParams = response.header("Location").split("/").last

          encodedParamStringShouldContain(base64UrlEncodedParams, expectedSuccessResultParams)

          val code = getFromEncodedParamString(base64UrlEncodedParams, "code")
          code.isDefined should be(true)
        }

        val expectedError = "invalid_request"
        val expectedErrorResultParams =
          Seq(
            ("client_id", validClientId),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
            ("error", expectedError)
          )

        get(uri = serverUri, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          val base64UrlEncodedParams = response.header("Location").split("/").last
          encodedParamStringShouldContain(base64UrlEncodedParams, expectedErrorResultParams)
        }
      }


      "sallii eri clientin käyttämään samaa code_challenge:a" in {
        val params = createValidAuthorizeParams
        val paramsString = createParamsString(params)
        val serverUri = s"${authorizeBaseUri}?${paramsString}"
        val expectedSuccessResultParams =
          Seq(
            ("client_id", validClientId),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
          )

        val paramsString2 = createParamsString((params.toMap + ("client_id" -> MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä.username)).toSeq)
        val serverUri2 = s"${authorizeBaseUri}?${paramsString2}"
        val expectedSuccessResultParams2 = (expectedSuccessResultParams.toMap + ("client_id" -> MockUsers.omadataOAuth2KaikkiOikeudetPalvelukäyttäjä.username)).toSeq

        get(uri = serverUri, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
          val base64UrlEncodedParams = response.header("Location").split("/").last
          encodedParamStringShouldContain(base64UrlEncodedParams, expectedSuccessResultParams)
          val code = getFromEncodedParamString(base64UrlEncodedParams, "code")
          code.isDefined should be(true)
        }

        get(uri = serverUri2, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
          val base64UrlEncodedParams = response.header("Location").split("/").last
          encodedParamStringShouldContain(base64UrlEncodedParams, expectedSuccessResultParams2)
          val code = getFromEncodedParamString(base64UrlEncodedParams, "code")
          code.isDefined should be(true)
        }
      }

      Seq("client_id", "redirect_uri").foreach(paramName => {
        s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} puuttuu" in {
          val väärälläParametrilla = createParamsString(createValidAuthorizeParams.filterNot(_._1 == paramName))
          val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
          }
        }

        s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} annettu enemmän kuin kerran" in {
          val validValue = createValidAuthorizeParams.toMap.get(paramName).get
          val väärälläParametrilla = createParamsString(createValidAuthorizeParams :+ (paramName, validValue))

          val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
          }
        }
      })

      "redirectaa logoutin kautta resource owner frontendiin, jos kutsutaan epävalidilla client_id:llä" in {
        val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

        val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }

      "redirectaa logoutin kautta resource owner frontendiin, jos kutsutaan epävalidilla redirect_uri:lla" in {
        val väärälläParametrilla = createParamsString((createValidAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

        val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }

      "kun halutaan välittää error" - {
        val validParamsWithError =
          createValidAuthorizeParams ++
            Seq(("error", "access_denied"))

        Seq("client_id", "redirect_uri").foreach(paramName => {
          s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} puuttuu" in {
            val väärälläParametrilla = createParamsString(validParamsWithError.filterNot(_._1 == paramName))
            val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
            }
          }

          s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} annettu enemmän kuin kerran" in {
            val validValue = createValidAuthorizeParams.toMap.get(paramName).get
            val väärälläParametrilla = createParamsString(validParamsWithError :+ (paramName, validValue))

            val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
            }
          }
        })

        "redirectaa logoutin kautta resource owner frontendiin, jos kutsutaan epävalidilla client_id:llä" in {
          val väärälläParametrilla = createParamsString((validParamsWithError.toMap + ("client_id" -> tuntematonClientId)).toSeq)

          val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"

          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
          }
        }

        "redirectaa logoutin kautta resource owner frontendiin, jos kutsutaan epävalidilla redirect_uri:lla" in {
          val väärälläParametrilla = createParamsString((validParamsWithError.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

          val serverUri = s"${authorizeBaseUri}?${väärälläParametrilla}"

          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
          }
        }

        "välittää virhetiedot clientille, jos client_id ja redirect_uri ovat kunnossa" in {
          val serverUri = s"${authorizeBaseUri}?${createParamsString(validParamsWithError)}"

          val expectedError = "access_denied"
          val expectedResultParamsString = createParamsString(
            Seq(
              ("client_id", validClientId),
              ("redirect_uri", validRedirectUri),
              ("state", validState),
              ("error", expectedError)
            )
          )

          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)

            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/${base64UrlEncode(expectedResultParamsString)}")
          }
        }
      }

      "Kun tallennus tietokantaan epäonnistuu, välittää virhetiedot clientille" in {
        def withOverridenErrorResult[T](f: => T): T =
          try {
            KoskiApplicationForTests.omaDataOAuth2Service.overridenCreateResultForUnitTests = Some(Left(OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, "dummy error")))
            f
          } finally {
            KoskiApplicationForTests.omaDataOAuth2Service.overridenCreateResultForUnitTests = None
          }

        val serverUri = s"${authorizeBaseUri}?${createParamsString(createValidAuthorizeParams)}"

        val expectedError = "server_error"
        val expectedParams =
          Seq(
            ("client_id", validClientId),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
            ("error", expectedError)
          )

        withOverridenErrorResult {
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
            val base64UrlEncodedParams = response.header("Location").split("/").last

            encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
          }
        }
      }

      "Kun parametreissa on virhe, joka olisi pitänyt käsitellä jo frontendiä avattaessa, kerrotaan virheestä frontendissä" in {
        // Selain/käyttäjä itse voi kutsua tätä routea väärillä parametreilla suoraan, siksi nämäkin tilanteet tulee käsitellä

        val parametriNimi = "scope"
        val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

        val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

        val serverUri = s"${authorizeBaseUri}?${eiSallitullaScopella}"

        val expectedParams =
          Seq(
            ("error", "invalid_scope")
          )

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")

          val base64UrlEncodedParams = response.header("Location").split("/").last
          encodedParamStringShouldContain(base64UrlEncodedParams, expectedParams)
          encodedParamStringShouldContainErrorUuidAsErrorId(base64UrlEncodedParams)
        }
      }
    }

    "clientilla ilman logouttia" - {
      "palauttaa authorization coden ilman logouttia" in {
        val serverUri = s"${authorizeBaseUri}?${createParamsString(validParamsVaihdetullaArvolla("client_id", validClientIdEiLogouttia))}"

        val expectedResultParams =
          Seq(
            ("client_id", validClientIdEiLogouttia),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
          )

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")

          val actualParams = Uri.unsafeFromString(response.header("Location")).params
          actualParams should contain allElementsOf (expectedResultParams)

          actualParams.get("code").isDefined should be(true)
        }
      }

      "tekee auditlokituksen" in {
        AuditLogTester.clearMessages()

        val serverUri = s"${authorizeBaseUri}?${createParamsString(validParamsVaihdetullaArvolla("client_id", validClientIdEiLogouttia))}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> "KANSALAINEN_MYDATA_LISAYS",
            "target" -> Map(
              "oppijaHenkiloOid" -> oppijaOid,
              "omaDataKumppani" -> validClientIdEiLogouttia,
              "omaDataOAuth2Scope" -> validScope
            ),
          ))
        }
      }

      "ei salli saman code_challenge:n käyttämistä monta kertaa" in {
        val paramsString = createParamsString(validParamsVaihdetullaArvolla("client_id", validClientIdEiLogouttia))
        val serverUri = s"${authorizeBaseUri}?${paramsString}"
        val expectedSuccessResultParams =
          Seq(
            ("client_id", validClientIdEiLogouttia),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
          )

        get(uri = serverUri, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")

          val actualParams = Uri.unsafeFromString(response.header("Location")).params
          actualParams should contain allElementsOf (expectedSuccessResultParams)

          actualParams.get("code").isDefined should be(true)
        }

        val expectedError = "invalid_request"
        val expectedErrorResultParams =
          Seq(
            ("client_id", validClientIdEiLogouttia),
            ("redirect_uri", validRedirectUri),
            ("state", validState),
            ("error", expectedError)
          )

        get(uri = serverUri, headers = kansalainenLoginHeaders(validKansalainen.hetu.get)) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")

          val actualParams = Uri.unsafeFromString(response.header("Location")).params
          actualParams should contain allElementsOf (expectedErrorResultParams)
        }
      }

      "kun halutaan välittää error" - {
        val validParamsWithError =
          validParamsVaihdetullaArvolla("client_id", validClientIdEiLogouttia) ++
            Seq(("error", "access_denied"))

        "välittää virhetiedot clientille, jos client_id ja redirect_uri ovat kunnossa" in {
          val serverUri = s"${authorizeBaseUri}?${createParamsString(validParamsWithError)}"

          val expectedError = "access_denied"
          val expectedResultParamsString = createParamsString(
            Seq(
              ("client_id", validClientIdEiLogouttia),
              ("redirect_uri", validRedirectUri),
              ("state", validState),
              ("error", expectedError)
            )
          )

          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/omadata-oauth2/post-response/?${expectedResultParamsString}")
          }
        }
      }
    }
  }

  "post-response -rajapinta" - {
    val validPostResponseParams =Seq(
      ("client_id", validClientId),
      ("redirect_uri", validRedirectUri),
      ("state", validState),
      ("code", validDummyCode)
    )

    Seq("client_id", "redirect_uri").foreach(paramName => {
      s"redirectaa resource owner frontendiin, kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validPostResponseParams.filterNot(_._1 == paramName))
        val serverUri = s"${postResponseBaseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }
    })

    Seq("client_id", "redirect_uri", "state").foreach(paramName => {
      s"redirectaa resource owner frontendiin, kun ${paramName} annettu enemmän kuin kerran" in {
        val validValue = createValidAuthorizeParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validPostResponseParams :+ (paramName, validValue))

        val serverUri = s"${postResponseBaseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }
    })


    "redirectaa resource owner frontendiin, jos kutsutaan epävalidilla client_id:llä" in {
      val väärälläParametrilla = createParamsString((validPostResponseParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

      val serverUri = s"${postResponseBaseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
      }
    }

    "redirectaa resource owner frontendiin, kun kutsutaan epävalidilla redirect_uri:lla" in {
      val väärälläParametrilla = createParamsString((validPostResponseParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

      val serverUri = s"${postResponseBaseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
      }
    }

    "välittää coden clientille, kun client_id ja redirect_uri ovat kunnossa" in {
      val expectedCode = validDummyCode

      val serverUri = s"${postResponseBaseUri}?${createParamsString(validPostResponseParams)}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(200)

        body should include(expectedCode)
        body should include(validState)
      }
    }

    "välittää virhetiedot clientille, kun client_id ja redirect_uri ovat kunnossa" in {
      val expectedError = "access_denied"

      val paramsWithError = (
          validPostResponseParams.toMap - "code" + ("error" -> expectedError)
      ).toSeq

      val serverUri = s"${postResponseBaseUri}?${createParamsString(paramsWithError)}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(200)

        body should include(expectedError)
        body should include(validState)
      }
    }
  }
}

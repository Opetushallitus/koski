package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.omadataoauth2.{OmaDataOAuth2Error, OmaDataOAuth2ErrorType}

class OmaDataOAuth2FrontendSpec extends OmaDataOAuth2TestBase {
  val app = KoskiApplicationForTests

  val tuntematonClientId = "loytymatonClientId"
  val vääräRedirectUri = "/koski/omadata-oauth2/EI-OLE/debug-post-response"

  "resource-owner authorize frontend -rajapinta" - {
    "toimivilla parametreilla" - {
      "kirjautumattomalla käyttäjällä" - {
        "redirectaa login-sivulle, joka redirectaa sivulle, jossa parametrit base64url-enkoodattuna" in {
          val serverUri = s"${authorizeFrontendBaseUri}?${validAuthorizeParamsString}"
          val expectedLoginUri = s"/koski/login/oppija?service=/koski/user/login?onSuccess=/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(validAuthorizeParamsString)}"

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
          val serverUri = s"${authorizeFrontendBaseUri}?${validAuthorizeParamsString}"
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

          val expectedLoginUri = s"/koski/login/oppija?service=/koski/user/login?onSuccess=/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(eiSallitullaScopella)}"

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

    "Suostumuksen myöntämis- ja datan haku-operaatiot audit-lokitetaan" in {
      // TODO: TOR-2210
    }

    "error-query parametrilla näyttää virheilmoituksen käyttäjälle" in {
      // TODO: TOR-2210
    }

    "viallisella client_id/redirect_uri:lla" - {
      "kirjautuneella käyttäjällä" - {
        "redirectaa käyttäjän samaan osoitteeseen query-parametreihin sisällytetyllä virheilmoituksella" - {
          Seq("client_id", "redirect_uri").foreach(paramName => {
            s"kun ${paramName} puuttuu" in {
              val väärälläParametrilla = createParamsString(validAuthorizeParams.filterNot(_._1 == paramName))
              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex(expectedErrorMessageRegexp)
              }
            }

            s"kun ${paramName} on annettu useammin kuin kerran" in {
              val validValue = validAuthorizeParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(validAuthorizeParams :+ (paramName, validValue))

              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex(expectedErrorMessageRegexp)
              }

            }
          })

          "kun client_id on tuntematon" in {
            val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex(expectedErrorMessageRegexp)
            }
          }

          "kun redirect_uri ei ole annetun client_idn tallennettu redirect_uri" in {
            val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex(expectedErrorMessageRegexp)
            }
          }
        }
      }

      "kirjautumattomalla käyttäjällä" - {
        "redirectaa käyttäjän samaan osoitteeseen query-parametreihin sisällytetyllä virheilmoituksella" - {
          Seq("client_id", "redirect_uri").foreach(paramName => {
            s"kun ${paramName} puuttuu" in {
              val väärälläParametrilla = createParamsString(validAuthorizeParams.filterNot(_._1 == paramName))
              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex(expectedErrorMessageRegexp)
              }
            }
          })

          Seq("client_id", "redirect_uri", "state").foreach(paramName => {
            s"kun ${paramName} on annettu useammin kuin kerran" in {
              val validValue = validAuthorizeParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(validAuthorizeParams :+ (paramName, validValue))

              val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

              val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include regex(expectedErrorMessageRegexp)
              }

            }
          })

          "kun client_id on tuntematon" in {
            val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex(expectedErrorMessageRegexp)
            }
          }

          "kun redirect_uri ei ole annetun client_idn tallennettu redirect_uri" in {
            val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex(expectedErrorMessageRegexp)
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

      // TODO: TOR-2210: testaa kirjatuneena ja kirjautumatta, molemmat
      "kirjautumattomana" - {

        "redirectaa virhetiedot palauttavalle post response -sivulle" - {
          muutPakollisetParametrinimet.foreach(paramName => {
            s"kun pakollinen parametri ${paramName} puuttuu kokonaan" in {
              val puuttuvallaParametrilla = createParamsString(validParamsIlman(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${puuttuvallaParametrilla}"

              get(
                uri = serverUri
              ) {
                verifyResponseStatus(302)
                response.header("Location") should not include(s"logout")
                response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
                response.header("Location") should include(s"error=invalid_request")
                response.header("Location") should include(queryStringUrlEncode(s"required parameter ${paramName} missing"))
                response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
              }
            }
          })

          muutPakollisetParametrinimet.foreach(paramName => {
            s"kun parametri ${paramName} esiintyy useamman kerran" in {
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
          })

          "kun response_type ei ole code" in {
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
          "kun response_mode ei ole form_post" in {
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
          "kun code_challenge_method ei ole S256" in {
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

          "kun scope on epävalidi" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} contains unknown scopes (HENKILOTIEDOT_VIRHEELLINEN)"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kun scopessa on toistensa kanssa epäyhteensopivia arvoja" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} contains an invalid combination of scopes (OPISKELUOIKEUDET_KAIKKI_TIEDOT, OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT)"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kun scopesta puuttuu opiskeluoikeudet-scope kokonaan" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should not include(s"logout")
              response.header("Location") should include(s"/koski/omadata-oauth2/post-response/")
              response.header("Location") should include(s"error=invalid_scope")
              response.header("Location") should include(queryStringUrlEncode(s"${parametriNimi}=${eiSallittuArvo} is missing a required OPISKELUOIKEUDET_ scope"))
              response.header("Location") should include regex("omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r)
            }
          }
          "kun scopesta puuttuu henkilötiedot-scope kokonaan" in {
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
          "kun code_challenge ei ole validimuotoinen challenge" in {
            // TODO: TOR-2210
          }
        }
      }

      "kirjautuneena" - {

        "redirectaa logoutin kautta virhetiedot palauttavalle post response -sivulle" - {

          muutPakollisetParametrinimet.foreach(paramName => {
            s"kun pakollinen parametri ${paramName} puuttuu kokonaan" in {
              val puuttuvallaParametrilla = createParamsString(validParamsIlman(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${puuttuvallaParametrilla}"

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
                // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
              }
            }
          })

          muutPakollisetParametrinimet.foreach(paramName => {
            s"kun parametri ${paramName} esiintyy useamman kerran" in {
              val duplikaattiParametrilla = createParamsString(validParamsDuplikaatilla(paramName))

              val serverUri = s"${authorizeFrontendBaseUri}?${duplikaattiParametrilla}"

              get(
                uri = serverUri,
                headers = kansalainenLoginHeaders(hetu)
              ) {
                verifyResponseStatus(302)
                response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
                // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
              }
            }
          })

          "kun response_type ei ole code" in {
            val parametriNimi = "response_type"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "code"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun response_mode ei ole form_post" in {
            val parametriNimi = "response_mode"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "form_post"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun code_challenge_method ei ole S256" in {
            val parametriNimi = "code_challenge_method"
            val tuntematonArvo = "tuntematon_arvo"
            val tuetutArvot = "S256"

            val väärälläParametrilla = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, tuntematonArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${väärälläParametrilla}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }

          "kun scope on epävalidi" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_VIRHEELLINEN OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun scopessa on toistensa kanssa epäyhteensopivia arvoja" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun scopesta puuttuu opiskeluoikeudet-scope kokonaan" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "HENKILOTIEDOT_NIMI HENKILOTIEDOT_HETU"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)

            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun scopesta puuttuu henkilötiedot-scope kokonaan" in {
            val parametriNimi = "scope"
            val eiSallittuArvo = "OPISKELUOIKEUDET_KAIKKI_TIEDOT"

            val eiSallitullaScopella = createParamsString(validParamsVaihdetullaArvolla(parametriNimi, eiSallittuArvo))

            val serverUri = s"${authorizeFrontendBaseUri}?${eiSallitullaScopella}"

            get(
              uri = serverUri,
              headers = kansalainenLoginHeaders(hetu)
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/post-response/")
              // TODO: TOR-2210: Base64url-enkoodattu osa Location-URLista pitäisi dekoodata ja tarkistaa sisältö
            }
          }
          "kun code_challenge ei ole validimuotoinen challenge" in {
            // TODO: TOR-2210
          }
        }
      }
    }

  }

  "resource-owner authorize -rajapinta" - {
    "ei toimi ilman kirjautumista" in {
      val serverUri = s"${authorizeBaseUri}?${validAuthorizeParamsString}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(401)
      }
    }

    "käyttäjän ollessa kirjautuneena" - {
      "palauttaa authorization code:n" in {
        val serverUri = s"${authorizeBaseUri}?${validAuthorizeParamsString}"

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

        val serverUri = s"${authorizeBaseUri}?${validAuthorizeParamsString}"

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
    }

    Seq("client_id", "redirect_uri").foreach(paramName => {
      s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validAuthorizeParams.filterNot(_._1 == paramName))
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
        val validValue = validAuthorizeParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validAuthorizeParams :+ (paramName, validValue))

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
      val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

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
      val väärälläParametrilla = createParamsString((validAuthorizeParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

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
        validAuthorizeParams ++
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
          val validValue = validAuthorizeParams.toMap.get(paramName).get
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

      val serverUri = s"${authorizeBaseUri}?${createParamsString(validAuthorizeParams)}"

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

      get(
        uri = serverUri,
        headers = kansalainenLoginHeaders(hetu)
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
        // TODO: TOR-2210: Toistaiseksi vain varmistetaan, että käyttäjä ohjataan frontendiin, ei tarkista välitettiinkö täsmälleen oikea virhesisältö
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
        val validValue = validAuthorizeParams.toMap.get(paramName).get
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

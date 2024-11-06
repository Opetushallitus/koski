package fi.oph.koski.omadataoauth2.unit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat

class OmaDataOAuth2FrontendSpec extends OmaDataOAuth2TestBase {
  val app = KoskiApplicationForTests

  val hetu = KoskiSpecificMockOppijat.eero.hetu.get

  val tuntematonClientId = "loytymatonClientId"
  val vääräRedirectUri = "/koski/omadata-oauth2/EI-OLE/debug-post-response"

  val validClientId = "oauth2client"
  val validState = "internal state"
  val validRedirectUri = "/koski/omadata-oauth2/debug-post-response"

  val validParams: Seq[(String, String)] = Seq(
    ("client_id", validClientId),
    ("response_type", "code"),
    ("response_mode", "form_post"),
    ("redirect_uri", validRedirectUri),
    ("code_challenge", "NjIyMGQ4NDAxZGM0ZDI5NTdlMWRlNDI2YWNhNjA1NGRiMjQyZTE0NTg0YzRmOGMwMmU3MzFkYjlhNTRlZTlmZA"),
    ("code_challenge_method", "S256"),
    ("state", validState),
    ("scope", "HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_NIMI OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT")
  )

  val validParamsString = createParamsString(validParams)

  "resource-owner authorize frontend -rajapinta" - {
    val baseUri = "omadata-oauth2/authorize"

    "toimivilla parametreilla" - {
      "kirjautumattomalla käyttäjällä" - {
        "redirectaa login-sivulle, joka redirectaa sivulle, jossa parametrit base64url-enkoodattuna" in {
          val serverUri = s"${baseUri}?${validParamsString}"
          val expectedLoginUri = s"/koski/login/oppija?service=/koski/user/login?onSuccess=/koski/omadata-oauth2/cas-workaround/authorize/${base64UrlEncode(validParamsString)}"

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
          val serverUri = s"${baseUri}?${validParamsString}"
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

          val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

          val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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
              val väärälläParametrilla = createParamsString(validParams.filterNot(_._1 == paramName))
              val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
              val validValue = validParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(validParams :+ (paramName, validValue))

              val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
            val väärälläParametrilla = createParamsString((validParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
            val väärälläParametrilla = createParamsString((validParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
              val väärälläParametrilla = createParamsString(validParams.filterNot(_._1 == paramName))
              val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
              val validValue = validParams.toMap.get(paramName).get
              val väärälläParametrilla = createParamsString(validParams :+ (paramName, validValue))

              val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
            val väärälläParametrilla = createParamsString((validParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

            val expectedErrorMessageRegexp = "error=invalid_client_data&error_id=omadataoauth2-error-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
            get(
              uri = serverUri
            ) {
              verifyResponseStatus(302)
              response.header("Location") should include regex(expectedErrorMessageRegexp)
            }
          }

          "kun redirect_uri ei ole annetun client_idn tallennettu redirect_uri" in {
            val väärälläParametrilla = createParamsString((validParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

              val serverUri = s"${baseUri}?${puuttuvallaParametrilla}"

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

              val serverUri = s"${baseUri}?${duplikaattiParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

              val serverUri = s"${baseUri}?${puuttuvallaParametrilla}"

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

              val serverUri = s"${baseUri}?${duplikaattiParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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

            val serverUri = s"${baseUri}?${eiSallitullaScopella}"

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
    val baseUri = "api/omadata-oauth2/resource-owner/authorize"

    "ei toimi ilman kirjautumista" in {
      val serverUri = s"${baseUri}?${validParamsString}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(401)
      }
    }

    "palauttaa authorization code:n käyttäjän ollessa kirjautuneena" in {
      val serverUri = s"${baseUri}?${validParamsString}"

      val expectedCode = validDummyCode
      val expectedResultParamsString = createParamsString(
        Seq(
          ("client_id", validClientId),
          ("redirect_uri", validRedirectUri),
          ("state", validState),
          ("code", expectedCode),
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

    Seq("client_id", "redirect_uri").foreach(paramName => {
      s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validParams.filterNot(_._1 == paramName))
        val serverUri = s"${baseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }

      s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} annettu enemmän kuin kerran" in {
        val validValue = validParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validParams :+ (paramName, validValue))

        val serverUri = s"${baseUri}?${väärälläParametrilla}"
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
      val väärälläParametrilla = createParamsString((validParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri,
        headers = kansalainenLoginHeaders(hetu)
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
      }
    }

    "redirectaa logoutin kautta resource owner frontendiin, jos kutsutaan epävalidilla redirect_uri:lla" in {
      val väärälläParametrilla = createParamsString((validParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

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
        validParams ++
          Seq(("error", "access_denied"))

      Seq("client_id", "redirect_uri").foreach(paramName => {
        s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} puuttuu" in {
          val väärälläParametrilla = createParamsString(validParamsWithError.filterNot(_._1 == paramName))
          val serverUri = s"${baseUri}?${väärälläParametrilla}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(302)
            response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
          }
        }

        s"redirectaa logoutin kautta resource owner frontendiin kun ${paramName} annettu enemmän kuin kerran" in {
          val validValue = validParams.toMap.get(paramName).get
          val väärälläParametrilla = createParamsString(validParamsWithError :+ (paramName, validValue))

          val serverUri = s"${baseUri}?${väärälläParametrilla}"
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

        val serverUri = s"${baseUri}?${väärälläParametrilla}"

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

        val serverUri = s"${baseUri}?${väärälläParametrilla}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(302)
          response.header("Location") should include(s"/koski/user/logout?target=/koski/omadata-oauth2/cas-workaround/authorize/")
        }
      }

      "välittää virhetiedot clientille, jos client_id ja redirect_uri ovat kunnossa" in {
        val serverUri = s"${baseUri}?${createParamsString(validParamsWithError)}"

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

    "TODO, virhetilanteessa X palautetaan callbackiin viesti Y" in {
      // TODO: TOR-2210
    }
  }

  "post-response -rajapinta" - {
    val baseUri = "omadata-oauth2/post-response"

    val validPostResponseParams =Seq(
      ("client_id", validClientId),
      ("redirect_uri", validRedirectUri),
      ("state", validState),
      ("code", validDummyCode)
    )

    Seq("client_id", "redirect_uri").foreach(paramName => {
      s"redirectaa resource owner frontendiin, kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validPostResponseParams.filterNot(_._1 == paramName))
        val serverUri = s"${baseUri}?${väärälläParametrilla}"
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
        val validValue = validParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validPostResponseParams :+ (paramName, validValue))

        val serverUri = s"${baseUri}?${väärälläParametrilla}"
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

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
      }
    }

    "redirectaa resource owner frontendiin, kun kutsutaan epävalidilla redirect_uri:lla" in {
      val väärälläParametrilla = createParamsString((validPostResponseParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(302)
        response.header("Location") should include(s"/koski/omadata-oauth2/cas-workaround/authorize/")
      }
    }

    "välittää coden clientille, kun client_id ja redirect_uri ovat kunnossa" in {
      val expectedCode = validDummyCode

      val serverUri = s"${baseUri}?${createParamsString(validPostResponseParams)}"

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

      val serverUri = s"${baseUri}?${createParamsString(paramsWithError)}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(200)

        body should include(expectedError)
        body should include(validState)
      }
    }
  }

  private def validParamsIlman(paramName: String): Seq[(String, String)] = {
    (validParams.toMap - paramName).toSeq
  }

  private def validParamsDuplikaatilla(paramName: String): Seq[(String, String)] = {
    val duplikaatti = validParams.toMap.get(paramName).get
    validParams ++ Seq((paramName, duplikaatti))
  }

  private def validParamsVaihdetullaArvolla(paramName: String, value: String): Seq[(String, String)] = {
    (validParams.toMap + (paramName -> value)).toSeq
  }
}

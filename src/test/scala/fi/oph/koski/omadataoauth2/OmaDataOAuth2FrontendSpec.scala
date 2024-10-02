package fi.oph.koski.omadataoauth2

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.{KoskiApplicationForTests}

class OmaDataOAuth2FrontendSpec extends OmaDataOAuth2TestBase {
  val app = KoskiApplicationForTests

  val hetu = KoskiSpecificMockOppijat.eero.hetu.get

  val tuntematonClientId = "loytymatonClientId"
  val vääräRedirectUri = "/koski/omadata-oauth2/EI-OLE/debug-post-response"

  val validClientId = "oauth2client"
  val validState = "internal state"
  val validRedirectUri = "/koski/omadata-oauth2/debug-post-response"

  val validParams = Seq(
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
      // TODO: TOR-2210: testaa kirjatuneena ja kirjautumatta, molemmat
      "redirectaa virhetiedot palauttavalle post response -sivulle" - {
        "kun on annettu duplikaattiparametreja" in {
          // TODO: TOR-2210
        }
        "kun response_type ei ole code" in {
          // TODO: TOR-2210
        }
        "kun response_mode ei ole form_post" in {
          // TODO: TOR-2210
        }
        "kun code_challenge_method ei ole S256" in {
          // TODO: TOR-2210
        }
        "kun scope on epävalidi" in {
          // TODO: TOR-2210
        }
        "kun scope ei ole annetulle clientille sallittu" in {
          // TODO: TOR-2210
        }
        "kun code_challenge ei ole validimuotoinen challenge" in {
          // TODO: TOR-2210
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
      s"palauttaa 500 kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validParams.filterNot(_._1 == paramName))
        val serverUri = s"${baseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(500)
        }
      }

      s"palauttaa 500 kun ${paramName} annettu enemmän kuin kerran" in {
        val validValue = validParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validParams :+ (paramName, validValue))

        val serverUri = s"${baseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(500)
        }
      }
    })

    "palauttaa 500, jos kutsutaan epävalidilla client_id:llä" in {
      val väärälläParametrilla = createParamsString((validParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri,
        headers = kansalainenLoginHeaders(hetu)
      ) {
        verifyResponseStatus(500)
      }
    }

    "palauttaa 500, jos kutsutaan epävalidilla redirect_uri:lla" in {
      val väärälläParametrilla = createParamsString((validParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri,
        headers = kansalainenLoginHeaders(hetu)
      ) {
        verifyResponseStatus(500)
      }
    }

    "kun halutaan välittää error" - {
      val validParamsWithError =
        validParams ++
          Seq(("error", "access_denied"))

      Seq("client_id", "redirect_uri").foreach(paramName => {
        s"palauttaa 500 kun ${paramName} puuttuu" in {
          val väärälläParametrilla = createParamsString(validParamsWithError.filterNot(_._1 == paramName))
          val serverUri = s"${baseUri}?${väärälläParametrilla}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(500)
          }
        }

        s"palauttaa 500 kun ${paramName} annettu enemmän kuin kerran" in {
          val validValue = validParams.toMap.get(paramName).get
          val väärälläParametrilla = createParamsString(validParamsWithError :+ (paramName, validValue))

          val serverUri = s"${baseUri}?${väärälläParametrilla}"
          get(
            uri = serverUri,
            headers = kansalainenLoginHeaders(hetu)
          ) {
            verifyResponseStatus(500)
          }
        }
      })

      "palauttaa 500, jos kutsutaan epävalidilla client_id:llä" in {
        val väärälläParametrilla = createParamsString((validParamsWithError.toMap + ("client_id" -> tuntematonClientId)).toSeq)

        val serverUri = s"${baseUri}?${väärälläParametrilla}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(500)
        }
      }

      "palauttaa 500, jos kutsutaan epävalidilla redirect_uri:lla" in {
        val väärälläParametrilla = createParamsString((validParamsWithError.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

        val serverUri = s"${baseUri}?${väärälläParametrilla}"

        get(
          uri = serverUri,
          headers = kansalainenLoginHeaders(hetu)
        ) {
          verifyResponseStatus(500)
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

    Seq("client_id", "redirect_uri").map(paramName => {
      s"palauttaa 500 kun ${paramName} puuttuu" in {
        val väärälläParametrilla = createParamsString(validPostResponseParams.filterNot(_._1 == paramName))
        val serverUri = s"${baseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri
        ) {
          verifyResponseStatus(500)
        }
      }

      s"palauttaa 500 kun ${paramName} annettu enemmän kuin kerran" in {
        val validValue = validParams.toMap.get(paramName).get
        val väärälläParametrilla = createParamsString(validPostResponseParams :+ (paramName, validValue))

        val serverUri = s"${baseUri}?${väärälläParametrilla}"
        get(
          uri = serverUri
        ) {
          verifyResponseStatus(500)
        }
      }
    })

    "palauttaa 500, jos kutsutaan epävalidilla client_id:llä" in {
      val väärälläParametrilla = createParamsString((validPostResponseParams.toMap + ("client_id" -> tuntematonClientId)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(500)
      }
    }

    "palauttaa 500, jos kutsutaan epävalidilla redirect_uri:lla" in {
      val väärälläParametrilla = createParamsString((validPostResponseParams.toMap + ("redirect_uri" -> vääräRedirectUri)).toSeq)

      val serverUri = s"${baseUri}?${väärälläParametrilla}"

      get(
        uri = serverUri
      ) {
        verifyResponseStatus(500)
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
}



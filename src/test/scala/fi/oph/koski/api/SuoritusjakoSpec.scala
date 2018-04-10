package fi.oph.koski.api

import java.nio.charset.StandardCharsets

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, SuoritusjakoResponse}
import org.scalatest.{FreeSpec, Matchers}

class SuoritusjakoSpec extends FreeSpec with SuoritusjakoTestMethods with Matchers {
  var secretYksiSuoritus: Option[String] = None
  var secretKaksiSuoritusta: Option[String] = None
  var secretVuosiluokanTuplausSuoritus: Option[String] = None

  "Suoritusjaon lisääminen" - {
    "onnistuu" - {
      "yhdellä oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        putSuoritusjako(json){
          verifyResponseStatusOk()
          secretYksiSuoritus = Option(JsonSerializer.parse[SuoritusjakoResponse](response.body).secret)
        }
      }

      "kahdella oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "6"
        }]"""
        putSuoritusjako(json) {
          verifyResponseStatusOk()
          secretKaksiSuoritusta = Option(JsonSerializer.parse[SuoritusjakoResponse](response.body).secret)
        }
      }

      "duplikoidulla suorituksella (vuosiluokan tuplaus)" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.14613773812",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        putSuoritusjako(json, hetu = "060498-997J"){
          verifyResponseStatusOk()
          secretVuosiluokanTuplausSuoritus = Option(JsonSerializer.parse[SuoritusjakoResponse](response.body).secret)
        }
      }
    }

    "epäonnistuu" - {
      "yhdellä suorituksella, jota ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        putSuoritusjako(json) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "kahdella suorituksella, joista toista ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        putSuoritusjako(json) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "puuttellisilla tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka"
        }]"""

        putSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "ylimääräisillä tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7",
          "extra": "extra"
        }]"""

        putSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "epäkelvolla JSON-dokumentilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7
        }]"""

        putSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
        }
      }

      "tyhjällä suorituslistalla" in {
        val json = "[]"

        putSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }
    }

    "tuottaa auditlog-merkinnän" in {
      AuditLogTester.clearMessages
      val json =
        """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

      putSuoritusjako(json){
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_LISAYS"))
      }
    }
  }

  "Suoritusjaon hakeminen" - {
    "onnistuu oikealla salaisuudella" in {
      getSuoritusjako(secretYksiSuoritus.get) {
        verifyResponseStatusOk()
      }
    }

    "epäonnistuu epäkelvolla salaisuudella" in {
      getSuoritusjako("2.2.246.562.10.64353470871") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
      }
    }

    "sisältää oikeat suoritukset" - {
      "yhden jaetun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secretYksiSuoritus.get)
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          oppilaitosOid = "1.2.246.562.10.64353470871",
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )))
      }

      "kahden jaetun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secretKaksiSuoritusta.get)
        verifySuoritusIds(oppija, List(
          SuoritusIdentifier(
            oppilaitosOid = "1.2.246.562.10.64353470871",
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "7"
          ),
          SuoritusIdentifier(
            oppilaitosOid = "1.2.246.562.10.64353470871",
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "6"
          )
        ))
      }

      "duplikoidun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secretVuosiluokanTuplausSuoritus.get)

        // Palautetaan vain yksi suoritus
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          oppilaitosOid = "1.2.246.562.10.14613773812",
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )))

        // Palautetaan tuplaus (ei luokallejäänti-suoritusta)
        oppija.opiskeluoikeudet.head.suoritukset.head match {
          case s: PerusopetuksenVuosiluokanSuoritus => !s.jääLuokalle && s.luokka == "7A"
          case _ => fail("Väärä palautettu suoritus")
        }
      }
    }

    "ei sisällä hetua" in {
      getSuoritusjako(secretYksiSuoritus.get) {
        verifyResponseStatusOk()
        val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)
        bodyString should not include(suoritusjakoHetu)
      }
    }

    "salaisuus ei päädy lokiin" in {
      val secret = secretYksiSuoritus.get
      val maskedSecret = secret.take(8) + "*" * (32 - 8)
      get(s"opinnot/$secret") {
        verifyResponseStatusOk()
        Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
        AccessLogTester.getLogMessages.lastOption.get.getMessage.toString should include(maskedSecret)
      }
    }

    "tuottaa auditlog-merkinnän" in {
      AuditLogTester.clearMessages
      getSuoritusjako(secretYksiSuoritus.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_KATSOMINEN"))
      }
    }
  }
}

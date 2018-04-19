package fi.oph.koski.api

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, Suoritusjako}
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.mutable

class SuoritusjakoSpec extends FreeSpec with SuoritusjakoTestMethods with Matchers {
  resetFixtures
  val secrets: mutable.Map[String, String] = mutable.Map()

  "Suoritusjaon lisääminen" - {
    "onnistuu" - {
      "yhdellä oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        createSuoritusjako(json){
          verifyResponseStatusOk()
          secrets += ("yksi suoritus" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
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

        createSuoritusjako(json) {
          verifyResponseStatusOk()
          secrets += ("kaksi suoritusta" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "duplikoidulla suorituksella (vuosiluokan tuplaus)" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.14613773812",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        createSuoritusjako(json, hetu = "060498-997J"){
          verifyResponseStatusOk()
          secrets += ("vuosiluokan tuplaus" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "lähdejärjestelmällisellä suorituksella" in {
        val json =
          """[{
           "lähdejärjestelmänId": "12345",
          "oppilaitosOid": "1.2.246.562.10.52251087186",
          "suorituksenTyyppi": "ammatillinentutkinto",
          "koulutusmoduulinTunniste": "351301"
        }]"""

        createSuoritusjako(json, hetu = "270303-281N"){
          verifyResponseStatusOk()
          secrets += ("lähdejärjestelmällinen" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
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

        createSuoritusjako(json) {
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

        createSuoritusjako(json) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "puuttellisilla tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka"
        }]"""

        createSuoritusjako(json) {
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

        createSuoritusjako(json) {
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

        createSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
        }
      }

      "tyhjällä suorituslistalla" in {
        val json = "[]"

        createSuoritusjako(json) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "tunnistautumattomalla käyttäjällä" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        createSuoritusjako(json, authenticate = false){
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
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

      createSuoritusjako(json){
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_LISAYS"))
        secrets += ("auditlog" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
      }
    }
  }

  "Suoritusjaon hakeminen" - {
    "onnistuu oikealla salaisuudella" in {
      getSuoritusjako(secrets("yksi suoritus")) {
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
        val oppija = getSuoritusjakoOppija(secrets("yksi suoritus"))
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = None,
          oppilaitosOid = "1.2.246.562.10.64353470871",
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )))
      }

      "kahden jaetun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secrets("kaksi suoritusta"))
        verifySuoritusIds(oppija, List(
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            oppilaitosOid = "1.2.246.562.10.64353470871",
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "7"
          ),
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            oppilaitosOid = "1.2.246.562.10.64353470871",
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "6"
          )
        ))
      }

      "duplikoidun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secrets("vuosiluokan tuplaus"))

        // Palautetaan vain yksi suoritus
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = None,
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

      "lähdejärjestelmällisellä suorituksella" in {
        val oppija = getSuoritusjakoOppija(secrets("lähdejärjestelmällinen"))
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = Some("12345"),
          oppilaitosOid = "1.2.246.562.10.52251087186",
          suorituksenTyyppi = "ammatillinentutkinto",
          koulutusmoduulinTunniste = "351301"
        )))
      }
    }

    "ei sisällä hetua" in {
      getSuoritusjako(secrets("yksi suoritus")) {
        verifyResponseStatusOk()
        val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)
        bodyString should not include(suoritusjakoHetu)
      }
    }

    "salaisuus ei päädy lokiin" in {
      val secret = secrets("yksi suoritus")
      val maskedSecret = secret.take(8) + "*" * (32 - 8)
      get(s"opinnot/$secret") {
        verifyResponseStatusOk()
        Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
        AccessLogTester.getLogMessages.lastOption.get.getMessage.toString should include(maskedSecret)
      }
    }

    "tuottaa auditlog-merkinnän" in {
      AuditLogTester.clearMessages
      getSuoritusjako(secrets("yksi suoritus")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_KATSOMINEN"))
      }
    }
  }

  "Suoritusjakolinkkien hakeminen" - {
    "onnistuu" - {
      "kun jakoja on olemassa" in {
        getSuoritusjakoDescriptors(){
          verifyResponseStatusOk()
        }
      }

      "kun jakoja ei ole olemassa" in {
        getSuoritusjakoDescriptors(hetu = "160932-311V"){
          verifyResponseStatusOk()
        }
      }
    }

    "sisältää" - {
      "kaikki jaot kun useita jakoja" in {
        val expirationDate = LocalDateTime.now().plusMonths(6).toLocalDate

        getSuoritusjakoDescriptors(){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("yksi suoritus"), expirationDate),
            Suoritusjako(secrets("kaksi suoritusta"), expirationDate),
            Suoritusjako(secrets("auditlog"), expirationDate)
          ))
        }
      }

      "yksittäisen jaon kun duplikoitu suoritus jaettu (vuosiluokan tuplaus)" in {
        val expirationDate = LocalDateTime.now().plusMonths(6).toLocalDate

        getSuoritusjakoDescriptors(hetu = "060498-997J"){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("vuosiluokan tuplaus"), expirationDate)
          ))
        }
      }

      "yksittäisen jaon kun lähdejärjestelmällinen suoritus jaettu" in {
        val expirationDate = LocalDateTime.now().plusMonths(6).toLocalDate

        getSuoritusjakoDescriptors(hetu = "270303-281N"){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("lähdejärjestelmällinen"), expirationDate)
          ))
        }
      }

      "tyhjän listan kun jakoja ei ole olemassa" in {
        getSuoritusjakoDescriptors(hetu = "160932-311V"){
          verifySuoritusjakoDescriptors(List())
        }
      }
    }

    "epäonnistuu tunnistautumattomalla käyttäjällä" in {
      getSuoritusjakoDescriptors(authenticate = false){
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
      }
    }
  }

  "Suoritusjaon päivämäärän muuttaminen" - {
    "onnistuu" - {
      "oikealla salaisuudella ja tulevalla päivämäärällä" in {
        val expirationDate = LocalDateTime.now().plusMonths(1).toLocalDate
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json){
          verifyResponseStatusOk()
          verifySuoritusjakoUpdate(expirationDate)
        }
      }
    }

    "epäonnistuu" - {
      "oikealla salaisuudella mutta menneellä päivämäärällä" in {
        val expirationDate = LocalDateTime.now().minusDays(1).toLocalDate
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest())
        }
      }

      "epäkelvolla salaisuudella" in {
        val expirationDate = LocalDateTime.now().plusMonths(1).toLocalDate
        val json =
          s"""{
          "secret": "2.2.246.562.10.64353470871",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json){
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "väärällä käyttäjällä" in {
        val expirationDate = LocalDateTime.now().plusMonths(1).toLocalDate
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json, "160932-311V"){
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "tunnistautumattomalla käyttäjällä" in {
        val expirationDate = LocalDateTime.now().plusMonths(1).toLocalDate
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json, authenticate = false){
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
        }
      }
    }
  }

  "Suoritusjaon poistaminen" - {
    "onnistuu" - {
      "oikealla salaisuudella" in {
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}"
        }"""

        deleteSuoritusjako(json){
          verifyResponseStatusOk()
        }
      }
    }

    "epäonnistuu" - {
      "epäkelvolla salaisuudella" in {
        val json =
          s"""{
          "secret": "2.2.246.562.10.64353470871"
        }"""

        deleteSuoritusjako(json){
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "väärällä käyttäjällä" in {
        val json =
          s"""{
          "secret": "${secrets("kaksi suoritusta")}"
        }"""

        deleteSuoritusjako(json, "160932-311V"){
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "tunnistautumattomalla käyttäjällä" in {
        val json =
          s"""{
          "secret": "${secrets("kaksi suoritusta")}"
        }"""

        deleteSuoritusjako(json, authenticate = false){
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
        }
      }
    }
  }
}

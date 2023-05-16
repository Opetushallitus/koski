package fi.oph.koski.api

import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.{Instant, LocalDate}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.schema.{KorkeakoulunOpiskeluoikeudenLukuvuosimaksu, Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu, PerusopetuksenVuosiluokanSuoritus, TäydellisetHenkilötiedot, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, Suoritusjako}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class SuoritusjakoSpec extends AnyFreeSpec with SuoritusjakoTestMethods with Matchers with OpiskeluoikeusTestMethodsAmmatillinen {
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

      "kahdella oikeellisella suorituksella joissa mukana opiskeluoikeuden oid" in {
        val oos = oppijaByHetu(suoritusjakoHetu).opiskeluoikeudet
        val seiskaluokka = oos.find(oo => oo.suoritukset.exists(s => s.koulutusmoduuli.tunniste.koodiarvo == "7")).get
        val kasiluokka = oos.find(oo => oo.suoritukset.exists(s => s.koulutusmoduuli.tunniste.koodiarvo == "8")).get

        val json =
          s"""[{
          "opiskeluoikeusOid": "${seiskaluokka.oid.get}",
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "opiskeluoikeusOid": "${kasiluokka.oid.get}",
          "oppilaitosOid": "1.2.246.562.10.14613773812",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "8"
        }]"""

        createSuoritusjako(json) {
          verifyResponseStatusOk()
          secrets += ("kaksi suoritusta opiskeluoikeuden oideilla" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "duplikoidulla suorituksella (vuosiluokan tuplaus)" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.14613773812",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        createSuoritusjako(json, hetu = "170186-6520"){
          verifyResponseStatusOk()
          secrets += ("vuosiluokan tuplaus" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "duplikoidulla suorituksella (vuosiluokan tuplaus) kun jaossa mukana opiskeluoikeuden oid" in {
        val oo = oppijaByHetu("170186-6520").opiskeluoikeudet.head
        val json =
          s"""[{
          "opiskeluoikeusOid": "${oo.oid.get}",
          "oppilaitosOid": "1.2.246.562.10.14613773812",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        createSuoritusjako(json, hetu = "170186-6520"){
          verifyResponseStatusOk()
          secrets += ("vuosiluokan tuplaus opiskeluoikeus oidilla" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "lähdejärjestelmällisellä suorituksella" in {
        val json =
          """[{
          "lähdejärjestelmänId": "l-050504",
          "oppilaitosOid": "1.2.246.562.10.52251087186",
          "suorituksenTyyppi": "ammatillinentutkinto",
          "koulutusmoduulinTunniste": "351301"
        }]"""

        createSuoritusjako(json, hetu = "270303-281N"){
          verifyResponseStatusOk()
          secrets += ("lähdejärjestelmällinen" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
        }
      }

      "vaikka virtakysely epäonnistuisikin" in {
        putOpiskeluoikeus(makeOpiskeluoikeus(), KoskiSpecificMockOppijat.virtaEiVastaa)(verifyResponseStatusOk())
        val json = """[{
          "oppilaitosOid": "1.2.246.562.10.52251087186",
          "suorituksenTyyppi": "ammatillinentutkinto",
          "koulutusmoduulinTunniste": "351301"
        }]"""

        val secret = createSuoritusjako(json, hetu = KoskiSpecificMockOppijat.virtaEiVastaa.hetu.get){
          verifyResponseStatusOk()
          JsonSerializer.parse[Suoritusjako](response.body).secret
        }

        getSuoritusjako(secret) {
          verifyResponseStatusOk()
        }
      }

      "vaikka oppilaitos puuttuisikin YTR-opinnoista" in {
        val json = """[{
          "suorituksenTyyppi": "ylioppilastutkinto",
          "koulutusmoduulinTunniste": "301000"
        }]"""

        val secret = createSuoritusjako(json, hetu = "280171-2730"){
          verifyResponseStatusOk()
          JsonSerializer.parse[Suoritusjako](response.body).secret
        }

        val oppija = getSuoritusjakoOppija(secret)
        val vahvistusPäivä = oppija.opiskeluoikeudet.head.asInstanceOf[YlioppilastutkinnonOpiskeluoikeus].suoritukset.head.vahvistus.get.päivä
        oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].sukunimi should equal("Seppänen")
        vahvistusPäivä should equal(LocalDate.of(1995, 5, 31))
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
          verifyResponseStatus(400, List(ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*missingProperty.*".r), ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*missingProperty.*".r)))
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
          verifyResponseStatus(400, List(ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*unexpectedProperty.*".r), ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*unexpectedProperty.*".r)))
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
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }

    "sisältää oikeat suoritukset" - {
      "yhden jaetun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secrets("yksi suoritus"))
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = None,
          opiskeluoikeusOid = None,
          oppilaitosOid = Some("1.2.246.562.10.64353470871"),
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )))
      }

      "kahden jaetun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secrets("kaksi suoritusta"))
        verifySuoritusIds(oppija, List(
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            opiskeluoikeusOid = None,
            oppilaitosOid = Some("1.2.246.562.10.64353470871"),
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "7"
          ),
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            opiskeluoikeusOid = None,
            oppilaitosOid = Some("1.2.246.562.10.64353470871"),
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "6"
          )
        ))
      }

      "kahden jaetun suorituksen salaisuudella kun jaossa mukana opiskeluoikeuden oid" in {
        val oppija = getSuoritusjakoOppija(secrets("kaksi suoritusta opiskeluoikeuden oideilla"))
        val seiskaluokka = oppija.opiskeluoikeudet.find(oo => oo.suoritukset.exists(s => s.koulutusmoduuli.tunniste.koodiarvo == "7")).get
        val kasiluokka = oppija.opiskeluoikeudet.find(oo => oo.suoritukset.exists(s => s.koulutusmoduuli.tunniste.koodiarvo == "8")).get

        verifySuoritusIds(oppija, List(
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            opiskeluoikeusOid = Some(seiskaluokka.oid.get),
            oppilaitosOid = Some("1.2.246.562.10.64353470871"),
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "7"
          ),
          SuoritusIdentifier(
            lähdejärjestelmänId = None,
            opiskeluoikeusOid = Some(kasiluokka.oid.get),
            oppilaitosOid = Some("1.2.246.562.10.14613773812"),
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "8"
          )
        ), checkOpiskeluoikeusOid = true)
      }

      "duplikoidun suorituksen salaisuudella" in {
        val oppija = getSuoritusjakoOppija(secrets("vuosiluokan tuplaus"))

        // Palautetaan vain yksi suoritus
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = None,
          opiskeluoikeusOid = None,
          oppilaitosOid = Some("1.2.246.562.10.14613773812"),
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )))

        // Palautetaan tuplaus (ei luokallejäänti-suoritusta)
        oppija.opiskeluoikeudet.head.suoritukset.head match {
          case s: PerusopetuksenVuosiluokanSuoritus => !s.jääLuokalle && s.luokka == "7A"
          case _ => fail("Väärä palautettu suoritus")
        }
      }

      "duplikoidun suorituksen salaisuudella kun mukana opiskeluoikeus oid" in {
        val oppija = getSuoritusjakoOppija(secrets("vuosiluokan tuplaus opiskeluoikeus oidilla"))

        // Palautetaan vain yksi suoritus
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = None,
          opiskeluoikeusOid = Some(oppija.opiskeluoikeudet.head.oid.get),
          oppilaitosOid = Some("1.2.246.562.10.14613773812"),
          suorituksenTyyppi = "perusopetuksenvuosiluokka",
          koulutusmoduulinTunniste = "7"
        )), checkOpiskeluoikeusOid = true)

        // Palautetaan tuplaus (ei luokallejäänti-suoritusta)
        oppija.opiskeluoikeudet.head.suoritukset.head match {
          case s: PerusopetuksenVuosiluokanSuoritus => !s.jääLuokalle && s.luokka == "7A"
          case _ => fail("Väärä palautettu suoritus")
        }
      }

      "lähdejärjestelmällisellä suorituksella" in {
        val oppija = getSuoritusjakoOppija(secrets("lähdejärjestelmällinen"))
        verifySuoritusIds(oppija, List(SuoritusIdentifier(
          lähdejärjestelmänId = Some("l-050504"),
          opiskeluoikeusOid = None,
          oppilaitosOid = Some("1.2.246.562.10.52251087186"),
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
      AccessLogTester.clearMessages
      val secret = secrets("yksi suoritus")
      val maskedSecret = secret.take(8) + "*" * (32 - 8)
      get(s"opinnot/$secret") {
        verifyResponseStatusOk()
        AccessLogTester.getLatestMatchingAccessLog("/koski/opinnot") should include(maskedSecret)
      }
    }

    "tuottaa auditlog-merkinnän" in {
      AuditLogTester.clearMessages
      getSuoritusjako(secrets("yksi suoritus")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_KATSOMINEN"))
      }
    }

    "ei sisällä korkeakoulun lukuvuosimaksutietoja" in {
      val json = """[{
          "lähdejärjestelmänId": 30759,
          "oppilaitosOid": "1.2.246.562.10.56753942459",
          "suorituksenTyyppi": "korkeakoulututkinto",
          "koulutusmoduulinTunniste": "651301"
        }]"""

      val secret = createSuoritusjako(json, hetu = "270191-4208"){
        verifyResponseStatusOk()
        JsonSerializer.parse[Suoritusjako](response.body).secret
      }

      getSuoritusjako(secret) {
        val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)

        bodyString should not include(Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu.toString.toLowerCase)
        bodyString should not include(KorkeakoulunOpiskeluoikeudenLukuvuosimaksu.toString.toLowerCase)
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
        val expirationDate = LocalDate.now.plusMonths(6)
        val timestamp = Timestamp.from(Instant.now())

        getSuoritusjakoDescriptors(){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("yksi suoritus"), expirationDate, timestamp),
            Suoritusjako(secrets("kaksi suoritusta"), expirationDate, timestamp),
            Suoritusjako(secrets("kaksi suoritusta opiskeluoikeuden oideilla"), expirationDate, timestamp),
            Suoritusjako(secrets("auditlog"), expirationDate, timestamp)
          ))
        }
      }

      "molemmat jaot kun duplikoitu suoritus jaettu (vuosiluokan tuplaus)" in {
        val expirationDate = LocalDate.now.plusMonths(6)
        val timestamp = Timestamp.from(Instant.now())

        getSuoritusjakoDescriptors(hetu = "170186-6520"){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("vuosiluokan tuplaus"), expirationDate, timestamp),
            Suoritusjako(secrets("vuosiluokan tuplaus opiskeluoikeus oidilla"), expirationDate, timestamp)
          ))
        }
      }

      "yksittäisen jaon kun lähdejärjestelmällinen suoritus jaettu" in {
        val expirationDate = LocalDate.now.plusMonths(6)
        val timestamp = Timestamp.from(Instant.now())

        getSuoritusjakoDescriptors(hetu = "270303-281N"){
          verifySuoritusjakoDescriptors(List(
            Suoritusjako(secrets("lähdejärjestelmällinen"), expirationDate, timestamp)
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
        val expirationDate = LocalDate.now.plusMonths(1)
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
        val expirationDate = LocalDate.now.minusDays(1)
        val json =
          s"""{
          "secret": "${secrets("yksi suoritus")}",
          "expirationDate": "${expirationDate.toString}"
        }"""

        updateSuoritusjako(json){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest())
        }
      }

      "oikealla salaisuudella mutta yli vuoden päässä olevalla päivämäärällä" in {
        val expirationDate = LocalDate.now.plusYears(1).plusDays(1)
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
        val expirationDate = LocalDate.now.plusMonths(1)
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
        val expirationDate = LocalDate.now.plusMonths(1)
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
        val expirationDate = LocalDate.now.plusMonths(1)
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

  "Suoritusjakojen määrän rajoitus" - {
    "Jaon lisääminen onnistuu kun käyttäjällä on alle maksimimäärä jakoja" in {
      resetFixtures

      val json =
        """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

      (1 to 100).foreach(_ =>
        createSuoritusjako(json){
          verifyResponseStatusOk()
        }
      )
    }

    "Jaon lisääminen epäonnistuu kun käyttäjällä on jo maksimimäärä jakoja" in {
      val json =
        """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

      createSuoritusjako(json){
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.liianMontaSuoritusjakoa())
      }
    }
  }
}

package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite}
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoitusLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDate.{of => date}

class ValpasKuntailmoitusApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    super.beforeEach()
    AuditLogTester.clearMessages
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    super.afterEach()
  }

  "Kuntailmoituksen tekeminen minimi-inputilla toimii" in {
    val minimiKuntailmoitus = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus", body = minimiKuntailmoitus, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()
    }
  }

  "Kuntailmoituksen tekeminen täydellä inputilla toimii" in {
    val kuntailmoitusInput = teeKuntailmoitusInputKaikillaTiedoilla()

    post("/valpas/api/kuntailmoitus", body = kuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()
    }
  }

  "Kuntailmoituksen ilmoituspäivä lisätään ilmoitukseen" in {
    val minimiKuntailmoitusInput = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus", body = minimiKuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()

      val responseKuntailmoitus = JsonSerializer.parse[ValpasKuntailmoitusLaajatTiedot](response.body)
      responseKuntailmoitus.aikaleima.map(_.toLocalDate) should equal(Some(FixtureUtil.DefaultTarkastelupäivä))
    }
  }

  "Kuntailmoituksen tekijän oid, sukunimi ja etunimet täydennetään ilmoituksen tietoihin käyttäjän tiedoista" in {
    val kuntailmoitusInput = teeKuntailmoitusInputTekijänYhteystiedoilla(
      etunimet = "foo oof",
      sukunimi = "bar",
      kutsumanimi = "foo",
      tekijäEmail = "foo@bar.com",
      tekijäPuhelinnumero = "050 999 9999"
    )

    post("/valpas/api/kuntailmoitus", body = kuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()

      val responseKuntailmoitus = JsonSerializer.parse[ValpasKuntailmoitusLaajatTiedot](response.body)

      val expectedTekijä = Some(ValpasKuntailmoituksenTekijäHenkilö(
        oid = Some(ValpasMockUsers.valpasJklNormaalikoulu.oid),
        etunimet = Some(ValpasMockUsers.valpasJklNormaalikoulu.firstname),
        sukunimi = Some(ValpasMockUsers.valpasJklNormaalikoulu.lastname),
        kutsumanimi = Some("foo"),
        email = Some("foo@bar.com"),
        puhelinnumero = Some("050 999 9999")))

      responseKuntailmoitus.tekijä.henkilö should equal(expectedTekijä)
    }
  }

  "Kuntailmoituksen tekijäorganisaation ja kunnan tiedot haetaan" in {
    val minimiKuntailmoitusInput = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus", body = minimiKuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()

      val responseKuntailmoitus = JsonSerializer.parse[ValpasKuntailmoitusLaajatTiedot](response.body)
      responseKuntailmoitus.tekijä.organisaatio.nimi should equal(
        Some(Finnish("Jyväskylän normaalikoulu",Some("Jyväskylän normaalikoulu"),Some("Jyväskylän normaalikoulu")))
      )
      responseKuntailmoitus.tekijä.organisaatio.kotipaikka should equal(
        Some(Koodistokoodiviite("179", Some(Finnish("Jyväskylä")), "kunta"))
      )
      responseKuntailmoitus.kunta.nimi should equal(Some(Finnish("Helsingin kaupunki",Some("Helsingfors stad"),None)))
      responseKuntailmoitus.kunta.kotipaikka should equal(
        Some(Koodistokoodiviite("091", Some(Finnish("Helsinki",Some("Helsingfors"), None)), "kunta"))
      )
    }
  }

  "Kuntailmoituksen yhteydenottokielen nimi täydentyy koodistosta" in {
    val minimiKuntailmoitusRuotsilla =
      s"""
         |{
         |  "oppijaOid": "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}",
         |  "kuntailmoitus" : {
         |    "tekijä" : {
         |      "organisaatio" : {
         |        "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |      }
         |    },
         |    "kunta" : {
         |      "oid" : "${MockOrganisaatiot.helsinginKaupunki}"
         |    },
         |    "oppijanYhteystiedot" : {},
         |    "hakenutMuualle" : false,
         |    "yhteydenottokieli" : {
         |      "koodiarvo" : "SV",
         |      "koodistoUri" : "kieli"
         |    }
         |  }
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus", body = minimiKuntailmoitusRuotsilla, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()

      val responseKuntailmoitus = JsonSerializer.parse[ValpasKuntailmoitusLaajatTiedot](response.body)

      responseKuntailmoitus.yhteydenottokieli should equal(
        Some(Koodistokoodiviite("SV", Some(Finnish("ruotsi", Some("svenska"), Some("Swedish"))), "kieli"))
      )
    }
  }

  "Kuntailmoituksen tekeminen luo rivin auditlogiin" in {
    val kuntailmoitusInput = teeKuntailmoitusInputKaikillaTiedoilla()

    post("/valpas/api/kuntailmoitus", body = kuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS.toString,
        "target" -> Map(
          ValpasAuditLogMessageField.oppijaHenkilöOid.toString ->
          ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
        )
      )
    }
  }

  "Kuntailmoitusta ei voi tehdä ennen lain voimaantuloa 1.8.2021" in {
    FixtureUtil.resetMockData(KoskiApplicationForTests, tarkastelupäivä = date(2021, 7, 31))

    val kuntailmoitusInput = teeKuntailmoitusInputKaikillaTiedoilla()

    post("/valpas/api/kuntailmoitus", body = kuntailmoitusInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(400, ValpasErrorCategory.validation.kuntailmoituksenIlmoituspäivä())
    }
  }

  "Kuntailmoituksen tekeminen palauttaa virheen rikkinäisellä JSONilla" in {
    val invalidJson =
      """{
        |  "foo: {
        |  }
        |}""".stripMargin

    post("/valpas/api/kuntailmoitus", body = invalidJson, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
    }
  }

  "Kuntailmoituksen tekeminen palauttaa schema-virheen validilla JSONilla, joka ei deserialisoidu kuntailmoitukseksi" in {
    val minimikuntailmoitusIlmanKuntaa =
      s"""
        |{
        |  "oppijaOid": "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}",
        |  "kuntailmoitus" : {
        |    "tekijä" : {
        |      "organisaatio" : {
        |        "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
        |      }
        |    }
        |  }
        |}
        |""".stripMargin

    post("/valpas/api/kuntailmoitus", body = minimikuntailmoitusIlmanKuntaa, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*kuntailmoitus.kunta.*missingProperty.*".r)
      )
    }
  }

  "Kuntailmoituksen tekeminen: Luontiyritys ilmoitus-id:llä epäonnistuu, koska ei voi muokata vanhaa, ainoastaan tehdä uuden" in {
    val minimiKuntailmoitusJossaId =
      s"""
         |{
         |  "oppijaOid": "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}",
         |  "kuntailmoitus" : {
         |    "id": 100,
         |    "tekijä" : {
         |      "organisaatio" : {
         |        "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |      }
         |    },
         |    "kunta" : {
         |      "oid" : "${MockOrganisaatiot.helsinginKaupunki}"
         |    }
         |  }
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus", body = minimiKuntailmoitusJossaId, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(501, ValpasErrorCategory.notImplemented.kuntailmoituksenMuokkaus())
    }
  }

  "Kuntailmoituksen tekeminen organisaatiopalvelusta puuttuvalla kunnalla palauttaa virheen" in {
    val minimikuntailmoitusVirheelliselläKuntaOidilla =
      teeMinimiKuntailmoitusInput(kuntaOid = "1.2.246.562.10.999999111000")

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusVirheelliselläKuntaOidilla,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(
          KoskiErrorCategory.badRequest.validation.jsonSchema,
          ".*Organisaatiota 1.2.246.562.10.999999111000 ei löydy organisaatiopalvelusta.*".r
        )
      )
    }
  }

  "Kuntailmoituksen tekeminen organisaatiopalvelusta puuttuvalla oppilaitoksella palauttaa virheen" in {
    val minimikuntailmoitusVirheelliselläOppilaitosOidilla =
      teeMinimiKuntailmoitusInput(tekijäOid = "1.2.246.562.10.999999111000")

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusVirheelliselläOppilaitosOidilla,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(
          KoskiErrorCategory.badRequest.validation.jsonSchema,
          ".*Organisaatiota 1.2.246.562.10.999999111000 ei löydy organisaatiopalvelusta.*".r
        )
      )
    }
  }

  "Kuntailmoituksen tekeminen käyttäen muuta kuin kuntaa kohdeorganisaationa palauttaa virheen" in {
    val minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena =
      teeMinimiKuntailmoitusInput(kuntaOid = MockOrganisaatiot.jyväskylänNormaalikoulu)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ValpasErrorCategory.validation.kuntailmoituksenKohde(
          s"Kuntailmoituksen kohde ${MockOrganisaatiot.jyväskylänNormaalikoulu} ei ole kunta"
        )
      )
    }
  }

  // TODO: Huom! Tämä hajoaa, kun kuntatoiminnallisuus toteutetaan. Toistaiseksi hyväksytään vain oppilaitokset,
  // joten tarkistetaan tämäkin vielä.
  "Kuntailmoituksen tekeminen käyttäen kuntaa tekijänä palauttaa virheen" in {
    val minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena =
      teeMinimiKuntailmoitusInput(tekijäOid = MockOrganisaatiot.helsinginKaupunki)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ValpasErrorCategory.validation.kuntailmoituksenTekijä(
          s"Kuntailmoituksen tekijä ${MockOrganisaatiot.helsinginKaupunki} ei ole oppilaitos"
        )
      )
    }
  }

  "Kuntailmoituksen tekeminen käyttäen muuta kuin oppilaitosta tekijänä palauttaa virheen" in {
    val minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena =
      teeMinimiKuntailmoitusInput(tekijäOid = MockOrganisaatiot.lehtikuusentienToimipiste)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusKäyttäenMuutaKuinKuntaaKohteena,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ValpasErrorCategory.validation.kuntailmoituksenTekijä(
          s"Kuntailmoituksen tekijä ${MockOrganisaatiot.lehtikuusentienToimipiste} ei ole oppilaitos"
        )
      )
    }
  }

  "Kuntailmoitusta ei voi tehdä toisen henkilön oidilla" in {
    val minimiKuntailmoitusJossaTekijänOid =
      s"""
         |{
         |  "oppijaOid": "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}",
         |  "kuntailmoitus" : {
         |    "tekijä" : {
         |      "organisaatio" : {
         |        "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |      },
         |      "henkilö" : {
         |        "oid" : "${ValpasMockUsers.valpasOphPääkäyttäjä.oid}"
         |      }
         |    },
         |    "kunta" : {
         |      "oid" : "${MockOrganisaatiot.helsinginKaupunki}"
         |    }
         |  }
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus",
      body = minimiKuntailmoitusJossaTekijänOid,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ValpasErrorCategory.validation.kuntailmoituksenTekijä("Kuntailmoitusta ei voi tehdä toisen henkilön oidilla")
      )
    }
  }

  "Kuntailmoituksen yhteydenottokieleksi ei voi valita muuta kuin FI tai SV" in {
    val minimiKuntailmoitusEpäkelvollaYhteydenottokielellä =
      s"""
         |{
         |  "oppijaOid": "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}",
         |  "kuntailmoitus" : {
         |    "tekijä" : {
         |      "organisaatio" : {
         |        "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |      }
         |    },
         |    "kunta" : {
         |      "oid" : "${MockOrganisaatiot.helsinginKaupunki}"
         |    },
         |    "yhteydenottokieli" : {
         |      "koodiarvo" : "EN",
         |      "koodistoUri" : "kieli"
         |    }
         |  }
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus",
      body = minimiKuntailmoitusEpäkelvollaYhteydenottokielellä,
      headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(
          KoskiErrorCategory.badRequest.validation.jsonSchema,
          ".*kuntailmoitus.yhteydenottokieli.koodiarvo.*enumValueMismatch.*".r
        )
      )
    }
  }

  "Kuntailmoituksen tekeminen ilman oikeuksia tekijäorganisaatioon palauttaa virheen" in {
    val minimikuntailmoitus = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitus,
      headers = authHeaders(ValpasMockUsers.valpasAapajoenKoulu) ++ jsonContent
    ) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.organisaatio(
          "Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä"
        )
      )
    }
  }

  "Kuntailmoituksen tekeminen hakeutumisen valvojana oppijalle, joka ei opiskele oppilaitoksessa lainkaan, palauttaa virheen" in {
    val minimikuntailmoitusAapajoenOppilaasta =
      teeMinimiKuntailmoitusInput(oppijaOid = ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusAapajoenOppilaasta,
      headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
    }
  }

  "Kuntailmoituksen tekeminen hakeutumisen valvojana oppijalle, joka opiskelee oppilaitoksessa vain oppivelvollisuuteen kelpaamattomia opintoja, palauttaa virheen" in {
    val minimikuntailmoitusJyväskylänEsikoululaisesta =
      teeMinimiKuntailmoitusInput(oppijaOid = ValpasMockOppijat.kulosaarenYsiluokkalainenJaJyväskylänEsikoululainen.oid)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusJyväskylänEsikoululaisesta,
      headers = authHeaders(ValpasMockUsers.valpasUseampiPeruskoulu) ++ jsonContent
    ) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta")
      )
    }
  }

  // TODO: Huom! Tämä hajoaa, kun muiden oppilaitosten toiminnallisuus. Toistaiseksi hyväksytään vain oppilaitokset,
  // joten tarkistetaan tämäkin vielä.
  "Kuntailmoituksen tekeminen hakeutumisen valvojana oppijalle, joka opiskelee oppilaitoksessa nivelvaiheen opintoja, palauttaa virheen" in {
    val minimikuntailmoitusJyväskylänNivelvaiheisesta =
      teeMinimiKuntailmoitusInput(oppijaOid = ValpasMockOppijat.kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen.oid)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusJyväskylänNivelvaiheisesta,
      headers = authHeaders(ValpasMockUsers.valpasUseampiPeruskoulu) ++ jsonContent
    ) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta")
      )
    }
  }

  // TODO: Huom! Tämä hajoaa, kun muiden oppilaitosten toiminnallisuus. Toistaiseksi hyväksytään vain oppilaitokset,
  // joten tarkistetaan tämäkin vielä.
  "Kuntailmoituksen tekeminen hakeutumisen valvojana oppijalle, joka opiskelee oppilaitoksessa toisen vaiheen opintoja, palauttaa virheen" in {
    val minimikuntailmoitusJyväskylänLukiolaisesta =
      teeMinimiKuntailmoitusInput(oppijaOid = ValpasMockOppijat.kulosaarenYsiluokkalainenJaJyväskylänLukiolainen.oid)

    post("/valpas/api/kuntailmoitus",
      body = minimikuntailmoitusJyväskylänLukiolaisesta,
      headers = authHeaders(ValpasMockUsers.valpasUseampiPeruskoulu) ++ jsonContent
    ) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta")
      )
    }
  }

  "Kuntailmoituksen tekeminen hakeutumisen valvojana oppijalle, jonka opiskelupaikkaan ei ole hakeutumisen valvonnan oikeuksia, palauttaa virheen" in {
    val minimiKuntailmoitus = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus",
      body = minimiKuntailmoitus,
      headers = authHeaders(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) ++ jsonContent
    ) {
      verifyResponseStatus(
        403,
        ValpasErrorCategory.forbidden.organisaatio(
          "Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä"
        )
      )
    }
  }

  "Kuntailmoituksen voi tehdä globaaleilla käyttöoikeuksilla kenelle vain oppijalle minkä vaan organisaation nimissä" in {
    val minimiKuntailmoitus = teeMinimiKuntailmoitusInput()

    post("/valpas/api/kuntailmoitus",
      body = minimiKuntailmoitus,
      headers = authHeaders(ValpasMockUsers.valpasOphPääkäyttäjä) ++ jsonContent
    ) {
      verifyResponseStatusOk()
    }
  }

  "Pohjatietojen haku audit-logitetaan oppijan tietojan katsomisena" in {
    val pohjatiedotInput =
      s"""
         |{
         |  "tekijäOrganisaatio" : {
         |    "oid" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |  },
         |  "oppijaOidit": [
         |    "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}"
         |  ]
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus/pohjatiedot", body = pohjatiedotInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatusOk()

      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KATSOMINEN.toString,
        "target" -> Map(
          ValpasAuditLogMessageField.oppijaHenkilöOid.toString ->
            ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      ))
    }
  }

  "Pohjatiedot palauttaa virheen rikkinäisellä JSONilla" in {
    val epävalidiPohjatiedotInput =
      s"""
         |{
         |  "tekijäOrganisaatio" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}"
         |  "oppijaOidit": [
         |    "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}"
         |  ]
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus/pohjatiedot",
      body = epävalidiPohjatiedotInput,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
    }
  }

  "Pohjatiedot palauttaa virheen ehjällä JSONilla, joka ei jäsenny Scala-olioksi" in {
    val pohjatiedotJossaPuutteellinenOrganisaatiorakenne =
      s"""
         |{
         |  "tekijäOrganisaatio" : "${MockOrganisaatiot.jyväskylänNormaalikoulu}",
         |  "oppijaOidit": [
         |    "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}"
         |  ]
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus/pohjatiedot",
      body = pohjatiedotJossaPuutteellinenOrganisaatiorakenne,
      headers = authHeaders() ++ jsonContent
    ) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*tekijäOrganisaatio.*notAnyOf.*".r)
      )
    }
  }

  "Pohjatiedot palauttaa virheen organisaatiolla, jota ei ole olemassa" in {
    val pohjatiedotInput =
      s"""
         |{
         |  "tekijäOrganisaatio" : {
         |    "oid" : "1.2.246.562.10.99999111112"
         |  },
         |  "oppijaOidit": [
         |    "${ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid}"
         |  ]
         |}
         |""".stripMargin

    post("/valpas/api/kuntailmoitus/pohjatiedot", body = pohjatiedotInput, headers = authHeaders() ++ jsonContent) {
      verifyResponseStatus(
        400,
        ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*Organisaatiota 1.2.246.562.10.99999111112 ei löydy organisaatiopalvelusta.*".r
        )
      )
    }
  }

  private def teeMinimiKuntailmoitusInput(
    oppijaOid: String = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
    tekijäOid: String = MockOrganisaatiot.jyväskylänNormaalikoulu,
    kuntaOid: String = MockOrganisaatiot.helsinginKaupunki
  ): String =
    s"""
       |{
       |  "oppijaOid": "${oppijaOid}",
       |  "kuntailmoitus" : {
       |    "tekijä" : {
       |      "organisaatio" : {
       |        "oid" : "${tekijäOid}"
       |      }
       |    },
       |    "kunta" : {
       |      "oid" : "${kuntaOid}"
       |    },
       |    "oppijanYhteystiedot" : {},
       |    "hakenutMuualle" : false
       |  }
       |}
       |""".stripMargin

  private def teeKuntailmoitusInputTekijänYhteystiedoilla(
    oppijaOid: String = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
    tekijäOid: String = MockOrganisaatiot.jyväskylänNormaalikoulu,
    tekijäEmail: String = "tekija@test.com",
    tekijäPuhelinnumero: String = "040 123 4567",
    etunimet: String = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    sukunimi: String = ValpasMockUsers.valpasJklNormaalikoulu.lastname,
    kutsumanimi: String = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    kuntaOid: String = MockOrganisaatiot.helsinginKaupunki
  ): String =
    s"""
       |{
       |  "oppijaOid": "${oppijaOid}",
       |  "kuntailmoitus" : {
       |    "tekijä" : {
       |      "organisaatio" : {
       |        "oid" : "${tekijäOid}"
       |      },
       |      "henkilö" : {
       |        "etunimet" : "${etunimet}",
       |        "sukunimi" : "${sukunimi}",
       |        "kutsumanimi" : "${kutsumanimi}",
       |        "email" : "${tekijäEmail}",
       |        "puhelinnumero" : "${tekijäPuhelinnumero}"
       |      }
       |    },
       |    "kunta" : {
       |      "oid" : "${kuntaOid}"
       |    },
       |    "oppijanYhteystiedot" : {},
       |    "hakenutMuualle" : false
       |  }
       |}
       |""".stripMargin


  private def teeKuntailmoitusInputKaikillaTiedoilla(
    oppijaOid: String = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
    tekijäOid: String = MockOrganisaatiot.jyväskylänNormaalikoulu,
    tekijäEmail: String = "tekija@test.com",
    tekijäPuhelinnumero: String = "040 123 4567",
    etunimet: String = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    sukunimi: String = ValpasMockUsers.valpasJklNormaalikoulu.lastname,
    kutsumanimi: String = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    kuntaOid: String = MockOrganisaatiot.helsinginKaupunki
  ): String = {
    s"""
       |{
       |  "oppijaOid": "${oppijaOid}",
       |  "kuntailmoitus" : {
       |    "tekijä" : {
       |      "organisaatio" : {
       |        "oid" : "${tekijäOid}"
       |      },
       |      "henkilö" : {
       |        "etunimet" : "${etunimet}",
       |        "sukunimi" : "${sukunimi}",
       |        "kutsumanimi" : "${kutsumanimi}",
       |        "email": "${tekijäEmail}",
       |        "puhelinnumero" : "${tekijäPuhelinnumero}"
       |      }
       |    },
       |    "kunta" : {
       |      "oid" : "${kuntaOid}"
       |    },
       |    "yhteydenottokieli" : {
       |      "koodiarvo" : "FI",
       |      "koodistoUri" : "kieli"
       |    },
       |    "oppijanYhteystiedot" : {
       |      "puhelinnumero" : "040 1234 567",
       |      "email" : "oppija.email@test.com",
       |      "lähiosoite" : "Metsäkatu 1 C 10",
       |      "postinumero" : "00100",
       |      "postitoimipaikka" : "Helsinki",
       |      "maa" : {
       |        "koodiarvo" : "246",
       |        "koodistoUri" : "maatjavaltiot2"
       |      }
       |    },
       |    "hakenutMuualle": false
       |  }
       |}
       |""".stripMargin

  }
}

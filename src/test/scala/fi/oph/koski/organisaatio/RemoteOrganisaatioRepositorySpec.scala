package fi.oph.koski.organisaatio

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{get, ok, okJson, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.http.Http
import fi.oph.koski.json.JsonResources.readResource
import fi.oph.koski.organisaatio.MockOrganisaatioRepository.hierarchyResourcename
import fi.oph.koski.organisaatio.MockOrganisaatiot.helsinginKaupunki
import fi.oph.koski.organisaatio.Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.write
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RemoteOrganisaatioRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll {
  implicit val jsonDefaultFormats: Formats = DefaultFormats.preservingEmptyValues
  implicit val cacheManager: CacheManager = GlobalCacheManager

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9877))
  private val orgRepository = new RemoteOrganisaatioRepository(Http("http://localhost:9877", "organisaatiopalvelu"), KoskiApplicationForTests.koodistoViitePalvelu)
  private val organisaatioHierarkiaJson  = readResource(hierarchyResourcename(Opetushallitus.organisaatioOid))

  "RemoteOrganisaatioRepository" - {
    "hakee koulutustoimijan organisaatiohierarkian" in {
      val hierarkia = orgRepository.getOrganisaatioHierarkia(helsinginKaupunki)
      hierarkia should be(MockOrganisaatioRepository.getOrganisaatioHierarkia(helsinginKaupunki))
      hierarkia.map(_.varhaiskasvatuksenJärjestäjä) should equal(Some(true))
    }

    "hakee oppilaitoksen organisaatiohierarkian" in {
      val hierarkia = orgRepository.getOrganisaatioHierarkia(MockOrganisaatiot.stadinAmmattiopisto)
      hierarkia should be(MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.stadinAmmattiopisto))
      hierarkia.map(_.varhaiskasvatuksenJärjestäjä) should equal(Some(false))
    }

    "hakee kaikki päiväkodit" in {
      val organisaatioHierarkia = organisaatioHierarkiaJson.extract[OrganisaatioHakuTulos].organisaatiot.map(MockOrganisaatioRepository.convertOrganisaatio(_))
      val päiväkotiCount = OrganisaatioHierarkia.flatten(organisaatioHierarkia).count(_.organisaatiotyypit.contains(VARHAISKASVATUKSEN_TOIMIPAIKKA))
      orgRepository.findAllVarhaiskasvatusToimipisteet.count(_.varhaiskasvatuksenOrganisaatioTyyppi) should equal(päiväkotiCount)
    }

    "hakee varhaiskasvatuksen toimipisteitä jotka eivät ole päiväkoteja" in {
      val organisaatioHierarkia = organisaatioHierarkiaJson.extract[OrganisaatioHakuTulos].organisaatiot.map(MockOrganisaatioRepository.convertOrganisaatio(_))
      val muuKuinPäiväkotiCount = OrganisaatioHierarkia.flatten(organisaatioHierarkia).count(o =>
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskouluasteenErityiskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.perusJaLukioasteenKoulut)
      )
      orgRepository.findAllVarhaiskasvatusToimipisteet.count(o => !o.varhaiskasvatuksenOrganisaatioTyyppi) should equal(muuKuinPäiväkotiCount)
    }

    "sähköposti virheiden raportointiin" - {
      "yhteystiedon kielen valinta" - {
        def valitse(yhteystiedot: List[(Option[String], String)], lang: String) =
          YhteystiedonKieli.valitseYhteystietoKielellä(yhteystiedot, lang)

        "valitsee yhteystiedon pyydetyllä kielellä järjestyksestä riippumatta" in {
          valitse(KaikkiKieletYhteystiedot, "fi") should equal(Some("koski.fi@example.com"))
          valitse(KaikkiKieletYhteystiedot, "sv") should equal(Some("koski.sv@example.com"))
          valitse(KaikkiKieletYhteystiedot, "en") should equal(Some("koski.en@example.com"))
        }

        "ei välitä kieli-koodiston versionumerosta" in {
          valitse(List((Some("kieli_fi#2"), "koski.fi@example.com")), "fi") should equal(Some("koski.fi@example.com"))
        }

        "palaa suomenkieliseen jos pyydettyä kieltä ei ole ilmoitettu" in {
          val eiRuotsia = List((Some("kieli_en#1"), "koski.en@example.com"), (Some("kieli_fi#1"), "koski.fi@example.com"))
          valitse(eiRuotsia, "sv") should equal(Some("koski.fi@example.com"))
        }

        "palaa fi-sv-en-järjestyksessä jos suomenkielistäkään ei ole" in {
          val vainRuotsiJaEnglanti = List((Some("kieli_en#1"), "koski.en@example.com"), (Some("kieli_sv#1"), "koski.sv@example.com"))
          valitse(vainRuotsiJaEnglanti, "fi") should equal(Some("koski.sv@example.com"))
        }

        "suosii kielellistä yhteystietoa kielettömän sijaan" in {
          valitse(List((None, "kieleton@example.com"), (Some("kieli_sv#1"), "koski.sv@example.com")), "sv") should equal(Some("koski.sv@example.com"))
        }

        "käyttää viimeisenä yhteystietoa jolle ei ole ilmoitettu kieltä" in {
          valitse(List((None, "kieleton@example.com")), "sv") should equal(Some("kieleton@example.com"))
        }

        "palauttaa None jos yhteystietoja ei ole" in {
          valitse(Nil, "fi") should equal(None)
        }
      }

      "organisaatiopalvelun vastauksesta" - {
        def email(oid: String, lang: String) =
          orgRepository.findSähköpostiVirheidenRaportointiin(oid, lang).map(_.email)

        "Koskea varten ilmoitetuista osoitteista valitaan asiointikielinen" in {
          email(KaikkiKieletOrg, "fi") should equal(Some("koski.fi@example.com"))
          email(KaikkiKieletOrg, "sv") should equal(Some("koski.sv@example.com"))
          email(KaikkiKieletOrg, "en") should equal(Some("koski.en@example.com"))
        }

        "organisaation yleisistä yhteystiedoista valitaan asiointikielinen" in {
          email(VainYleisetYhteystiedotOrg, "fi") should equal(Some("yleinen.fi@example.com"))
          email(VainYleisetYhteystiedotOrg, "en") should equal(Some("yleinen.en@example.com"))
          email(VainYleisetYhteystiedotOrg, "sv") should equal(Some("yleinen.fi@example.com"))
        }

        "Koskea varten ilmoitettu osoite voittaa yleisen yhteystiedon myös eri kielellä" in {
          email(KoskiOsoiteVainRuotsiksiOrg, "fi") should equal(Some("koski.sv@example.com"))
        }

        "tyhjä Koski-osoite ei estä yleisen yhteystiedon käyttöä" in {
          email(TyhjaKoskiOsoiteOrg, "fi") should equal(Some("yleinen.fi@example.com"))
        }

        "parent-organisaatiolta haettaessa käytetään samaa asiointikieltä" in {
          email(EiOsoitettaOrg, "sv") should equal(Some("koski.sv@example.com"))
        }
      }
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    mockEndpoints
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private val YhteystietojenTyyppiKoski = "1.2.246.562.5.79385887983"

  private val KaikkiKieletOrg = "1.2.246.562.10.00000000101"
  private val VainYleisetYhteystiedotOrg = "1.2.246.562.10.00000000102"
  private val KoskiOsoiteVainRuotsiksiOrg = "1.2.246.562.10.00000000103"
  private val EiOsoitettaOrg = "1.2.246.562.10.00000000104"
  private val TyhjaKoskiOsoiteOrg = "1.2.246.562.10.00000000105"

  // Organisaatiopalvelu ei palauta yhteystietoja kielen mukaisessa järjestyksessä, joten
  // englanninkielinen osoite on fixtureissa tarkoituksella ensimmäisenä.
  private val KaikkiKieletYhteystiedot: List[(Option[String], String)] = List(
    (Some("kieli_en#1"), "koski.en@example.com"),
    (Some("kieli_fi#1"), "koski.fi@example.com"),
    (Some("kieli_sv#1"), "koski.sv@example.com")
  )

  private def jsonObject(fields: (String, String)*): String =
    fields.map { case (k, v) => s""""$k": "$v"""" }.mkString("{", ", ", "}")

  private def koskiYhteystietoArvo(kieli: Option[String], email: String): String =
    jsonObject(
      List(
        "YhteystietoArvo.arvoText" -> email,
        "YhteystietoElementti.tyyppi" -> "Email",
        "YhteystietojenTyyppi.oid" -> YhteystietojenTyyppiKoski,
        "YhteystietoElementti.kaytossa" -> "true"
      ) ++ kieli.map(k => "YhteystietoArvo.kieli" -> k): _*
    )

  private def yleinenYhteystieto(kieli: Option[String], email: String): String =
    jsonObject(List("email" -> email) ++ kieli.map(k => "kieli" -> k): _*)

  private def organisaatioV3Json(
    oid: String,
    koskiOsoitteet: List[(Option[String], String)] = Nil,
    yleisetOsoitteet: List[(Option[String], String)] = Nil,
    parentOid: Option[String] = None
  ): String =
    s"""{
       |  "oid": "$oid",
       |  "nimi": { "fi": "Testiorganisaatio", "sv": "Testorganisation", "en": "Test organisation" },
       |  "status": "AKTIIVINEN",
       |  ${parentOid.map(p => s""""parentOid": "$p",""").getOrElse("")}
       |  "yhteystiedot": [${yleisetOsoitteet.map { case (kieli, email) => yleinenYhteystieto(kieli, email) }.mkString(", ")}],
       |  "yhteystietoArvos": [${koskiOsoitteet.map { case (kieli, email) => koskiYhteystietoArvo(kieli, email) }.mkString(", ")}]
       |}""".stripMargin

  private def stubOrganisaatioV3(oid: String, json: String): Unit =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/organisaatio-service/rest/organisaatio/v3/$oid")).willReturn(okJson(json)))

  private def mockEndpoints = {
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/organisaatio-service/rest/organisaatio/v4/${Opetushallitus.organisaatioOid}/jalkelaiset"))
        .willReturn(ok(write(organisaatioHierarkiaJson))))

    stubOrganisaatioV3(KaikkiKieletOrg, organisaatioV3Json(
      KaikkiKieletOrg,
      koskiOsoitteet = KaikkiKieletYhteystiedot
    ))
    stubOrganisaatioV3(VainYleisetYhteystiedotOrg, organisaatioV3Json(
      VainYleisetYhteystiedotOrg,
      yleisetOsoitteet = List(
        (Some("kieli_en#1"), "yleinen.en@example.com"),
        (Some("kieli_fi#1"), "yleinen.fi@example.com")
      )
    ))
    stubOrganisaatioV3(KoskiOsoiteVainRuotsiksiOrg, organisaatioV3Json(
      KoskiOsoiteVainRuotsiksiOrg,
      koskiOsoitteet = List((Some("kieli_sv#1"), "koski.sv@example.com")),
      yleisetOsoitteet = List((Some("kieli_fi#1"), "yleinen.fi@example.com"))
    ))
    stubOrganisaatioV3(EiOsoitettaOrg, organisaatioV3Json(EiOsoitettaOrg, parentOid = Some(KaikkiKieletOrg)))
    stubOrganisaatioV3(TyhjaKoskiOsoiteOrg, organisaatioV3Json(
      TyhjaKoskiOsoiteOrg,
      koskiOsoitteet = List((Some("kieli_fi#1"), "")),
      yleisetOsoitteet = List((Some("kieli_fi#1"), "yleinen.fi@example.com"))
    ))
  }
}

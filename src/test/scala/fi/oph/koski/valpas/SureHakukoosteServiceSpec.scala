package fi.oph.koski.valpas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.Fault
import com.typesafe.config.ConfigFactory
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.valpas.hakukooste.{Hakukooste, HakukoosteExampleData, ValpasHakukoosteService}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, EitherValues}

import java.time.LocalDateTime

class SureHakukoosteServiceSpec extends ValpasTestBase with Matchers with EitherValues with BeforeAndAfterAll {
  implicit val jsonDefaultFormats: Formats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9875"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
      |valpas.hakukoosteEnabled = true
      |valpas.hakukoosteTimeoutSeconds = 2
    """.stripMargin)

  private val mockClient = ValpasHakukoosteService(KoskiApplicationForTests, Some(config))

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9875))

  private val sureHakukoosteUrl = "/suoritusrekisteri/rest/v1/valpas/"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "SureHakukoosteService" - {
    "käsittelee virhetilanteen kun suoritusrekisteri ei vastaa" in {
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(WireMock.status(500)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "käsittelee virhetilanteen kun vastaus ei vastaa schemaa" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockResponseForOids(queryOids, hakukooste => hakukooste.copy(
        hakutoiveet = hakukooste.hakutoiveet.map(_.copy(valintatila = Some("kielletty arvo")))
      ))
      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(502)
      result.errorString.get should startWith("Suoritusrekisterin palauttama hakukoostetieto oli viallinen")
    }

    "toimii kun vastaus katkeaa kesken" in {
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "toimii kun vastauksessa kestää liian kauan" in {
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(aResponse().withFixedDelay(10000)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value

      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "toimii kun vastaus on tyhjä" in {
      val queryOids = Set("asdf")
      mockResponseForOids(queryOids)
      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value
      result.map(_.oppijaOid) should equal(List.empty)
    }

    "palauttaa hakukoostetiedot kun oid löytyy" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockResponseForOids(queryOids)
      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value
      result.map(_.oppijaOid) should equal(queryOids.toList)
    }

    "osaa serialisoida aidon API-vastauksen" in {
      val queryOids = Set("1.2.246.562.24.85292063498")
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(WireMock.ok().withBody(SureHakukoosteServiceSpec.realResponse()))
      )
      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value.head
      result.oppijaOid should equal(queryOids.head)
      result.hakutapa.nimi.get.get("sv") should equal("Gemensam ansökan")
      result.hakuNimi.get("en") should startWith("Joint Application")
      result.hakutoiveet.map(_.hakukohdeOid) should equal(List("1.2.246.562.20.80878445842", "1.2.246.562.20.24492106752"))
      result.haunAlkamispaivamaara should equal(LocalDateTime.of(2021, 4, 20, 9, 0))
    }

    "ei osaa serialisoida API vastausta, jossa olemattomia koodiarvoja" in {
      val queryOids = Set("1.2.246.562.24.85292063498")
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(WireMock.ok().withBody(SureHakukoosteServiceSpec.realResponse("01#1")))
      )
      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(502)
      result.errorString.get should startWith("Suoritusrekisterin palauttama hakukoostetieto oli viallinen")
    }
  }

  private def mockResponseForOids(
    queryOids: Set[String],
    alterResponse: Hakukooste => Hakukooste = identity
  ): Unit = {
    val expectedRequest = queryOids.map(oid => oid.mkString("\"", "", "\"")).mkString("[", ",", "]")
    val response = HakukoosteExampleData.data
      .filter(entry => queryOids.contains(entry.oppijaOid))
      .map(alterResponse)

    wireMockServer.stubFor(
      WireMock.post(WireMock.urlPathEqualTo(sureHakukoosteUrl))
        .withRequestBody(WireMock.equalToJson(expectedRequest))
        .willReturn(WireMock.ok().withBody(JsonSerializer.writeWithRoot(response))))
  }
}

object SureHakukoosteServiceSpec {
  private def realResponse(hakutapaKoodiarvo: String = "01") =
    s"""[
      |  {
      |    "hakutapa": {
      |      "koodiarvo": "${hakutapaKoodiarvo}",
      |      "nimi": {
      |        "fi": "Yhteishaku",
      |        "sv": "Gemensam ansökan"
      |      },
      |      "lyhytNimi": {
      |        "fi": "YH",
      |        "sv": "GA",
      |        "en": ""
      |      },
      |      "koodistoUri": "hakutapa",
      |      "koodistoVersio": 1
      |    },
      |    "hakutyyppi": {
      |      "koodiarvo": "01",
      |      "nimi": {
      |        "fi": "Varsinainen haku",
      |        "sv": "Egentlig ansökan"
      |      },
      |      "lyhytNimi": {
      |        "fi": "VH",
      |        "sv": "EA"
      |      },
      |      "koodistoUri": "hakutyyppi",
      |      "koodistoVersio": 1
      |    },
      |    "haunAlkamispaivamaara": "2021-04-20T09:00:00",
      |    "aktiivinenHaku": true,
      |    "oppijaOid": "1.2.246.562.24.85292063498",
      |    "hakemusOid": "1.2.246.562.11.00000000000000675952",
      |    "hakemusUrl": "/placeholder-hakemus-url",
      |    "hakuOid": "1.2.246.562.29.72389663526",
      |    "hakuNimi": {
      |      "fi": "Korkeakoulujen kevään 2021 ensimmäinen yhteishaku",
      |      "sv": "Högskolornas första gemensamma ansökan, våren 2021",
      |      "en": "Joint Application to Higher Education, Spring 2021"
      |    },
      |    "email": "hakija-33809716-muokkaus@oph.fi",
      |    "matkapuhelin": "050 86732551",
      |    "postinumero": "00100",
      |    "lahiosoite": "Kinkkuaukio 275",
      |    "postitoimipaikka": "Olsztyn Poland",
      |    "hakutoiveet": [
      |      {
      |        "hakukohdeNimi": {},
      |        "koulutusNimi": {},
      |        "vastaanottotieto": "KESKEN",
      |        "valintatila": "KESKEN",
      |        "ilmoittautumistila": "EI_TEHTY",
      |        "hakutoivenumero": 0,
      |        "hakukohdeOid": "1.2.246.562.20.80878445842",
      |        "hakukohdeKoulutuskoodi": {
      |          "koodiarvo": "751201",
      |          "nimi": {
      |            "fi": "Dipl.ins., sähkötekniikka"
      |          },
      |          "lyhytNimi": {
      |            "fi": "DI sähkötekniikka"
      |          },
      |          "koodistoUri": "koulutus",
      |          "koodistoVersio": 1
      |        },
      |        "hakukohdeOrganisaatio": "1.2.246.562.10.38864316104",
      |        "organisaatioNimi": {},
      |        "koulutusOid": "1.2.246.562.17.83264128444",
      |        "valintakoe": []
      |      },
      |      {
      |        "hakukohdeNimi": {},
      |        "koulutusNimi": {},
      |        "vastaanottotieto": "KESKEN",
      |        "valintatila": "KESKEN",
      |        "ilmoittautumistila": "EI_TEHTY",
      |        "hakutoivenumero": 1,
      |        "hakukohdeOid": "1.2.246.562.20.24492106752",
      |        "hakukohdeKoulutuskoodi": {
      |          "koodiarvo": "751301",
      |          "nimi": {
      |            "fi": "Dipl.ins., tietotekniikka"
      |          },
      |          "lyhytNimi": {
      |            "fi": "DI tietotekniikka"
      |          },
      |          "koodistoUri": "koulutus",
      |          "koodistoVersio": 1
      |        },
      |        "hakukohdeOrganisaatio": "1.2.246.562.10.39920288212",
      |        "organisaatioNimi": {},
      |        "koulutusOid": "1.2.246.562.17.58545683169"
      |      }
      |    ]
      |  }
      |]""".stripMargin
}

package fi.oph.koski.valpas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.valpas.hakukooste.{Hakukooste, HakukoosteExampleData, ValpasHakukoosteService}
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import org.json4s.{DefaultFormats, Formats}
import org.scalatest._

import java.time.LocalDateTime

class SureHakukoosteServiceSpec extends ValpasTestBase with Matchers with EitherValues with BeforeAndAfterAll {
  implicit val jsonDefaultFormats: Formats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9875"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
    """.stripMargin)

  private val mockClient = ValpasHakukoosteService(config)

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9875))

  private val sureHakukoosteUrl = "/suoritusrekisteri/rest/v1/valpas/"

  override def beforeAll() {
    wireMockServer.start()
    super.beforeAll()
  }

  override def afterAll() {
    wireMockServer.stop()
    super.afterAll()
  }

  "SureHakukoosteService" - {
    "käsittelee virhetilanteen kun suoritusrekisteri ei vastaa" in {
      wireMockServer.stubFor(
        WireMock.post(urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(status(500)))

      val result = mockClient.getHakukoosteet(Set("asdf")).left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "käsittelee virhetilanteen kun vastaus ei vastaa schemaa" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockResponseForOids(queryOids, hakukooste => hakukooste.copy(
        hakutoiveet = hakukooste.hakutoiveet.map(_.copy(valintatila = Some("kielletty arvo")))
      ))
      val result = mockClient.getHakukoosteet(queryOids).left.value
      result.statusCode should equal(500)
      result.errorString.get should startWith("Internal server error")
    }

    "toimii kun vastaus on tyhjä" in {
      val queryOids = Set("asdf")
      mockResponseForOids(queryOids)
      val result = mockClient.getHakukoosteet(queryOids).right.value
      result.map(_.oppijaOid) should equal(List.empty)
    }

    "palauttaa hakukoostetiedot kun oid löytyy" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockResponseForOids(queryOids)
      val result = mockClient.getHakukoosteet(queryOids).right.value
      result.map(_.oppijaOid) should equal(queryOids.toList)
    }

    "osaa serialisoida aidon API-vastauksen" in {
      val queryOids = Set("1.2.246.562.24.85292063498")
      wireMockServer.stubFor(
        WireMock.post(urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(ok().withBody(SureHakukoosteServiceSpec.realResponse))
      )
      val result = mockClient.getHakukoosteet(queryOids).right.value.head
      result.oppijaOid should equal(queryOids.head)
      result.hakutapa.nimi.get.get("sv") should equal("Gemensam ansökan")
      result.hakuNimi.get("en") should startWith("Joint Application")
      result.hakutoiveet.map(_.hakukohdeOid) should equal(List("1.2.246.562.20.80878445842", "1.2.246.562.20.24492106752"))
      result.haunAlkamispaivamaara should equal(LocalDateTime.of(2021, 4, 20, 9, 0))
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
      WireMock.post(urlPathEqualTo(sureHakukoosteUrl))
        .withRequestBody(equalToJson(expectedRequest))
        .willReturn(ok().withBody(JsonSerializer.writeWithRoot(response))))
  }
}

object SureHakukoosteServiceSpec {
  private val realResponse =
    """[
      |  {
      |    "hakutapa": {
      |      "koodiarvo": "01",
      |      "nimi": {
      |        "fi": "Yhteishaku",
      |        "sv": "Gemensam ansökan"
      |      },
      |      "lyhytNimi": {
      |        "fi": "YH",
      |        "sv": "GA"
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
      |        "hakukohdeKoulutuskoodi": "751201",
      |        "hakukohdeOrganisaatio": "1.2.246.562.10.38864316104",
      |        "koulutusOid": "1.2.246.562.17.83264128444"
      |      },
      |      {
      |        "hakukohdeNimi": {},
      |        "koulutusNimi": {},
      |        "vastaanottotieto": "KESKEN",
      |        "valintatila": "KESKEN",
      |        "ilmoittautumistila": "EI_TEHTY",
      |        "hakutoivenumero": 1,
      |        "hakukohdeOid": "1.2.246.562.20.24492106752",
      |        "hakukohdeKoulutuskoodi": "751301",
      |        "hakukohdeOrganisaatio": "1.2.246.562.10.39920288212",
      |        "koulutusOid": "1.2.246.562.17.58545683169"
      |      }
      |    ]
      |  }
      |]""".stripMargin
}

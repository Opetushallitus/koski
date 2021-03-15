package fi.oph.koski.valpas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import fi.oph.koski.valpas.hakukooste.{HakukoosteExampleData, ValpasHakukoosteService}
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, Formats}
import org.scalatest._

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

  override def beforeAll {
    wireMockServer.start()
    super.beforeAll()
  }

  override def afterAll {
    wireMockServer.stop()
    super.afterAll()
  }

  "SureHakukoosteService" - {
    "käsittelee virhetilanteen" in {
      wireMockServer.stubFor(
        WireMock.post(urlPathEqualTo(sureHakukoosteUrl))
          .willReturn(status(500)))

      val result = mockClient.getHakukoosteet(Set("asdf")).left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
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
  }

  private def mockResponseForOids(queryOids: Set[String]): Unit = {
    val expectedRequest = queryOids.map(oid => oid.mkString("\"", "", "\"")).mkString("[", ",", "]")
    val response = HakukoosteExampleData.data.filter(entry => queryOids.contains(entry.oppijaOid))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(sureHakukoosteUrl))
        .withRequestBody(equalToJson(expectedRequest))
        .willReturn(ok().withBody(write(response))))
  }
}

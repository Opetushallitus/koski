package fi.oph.koski.organisaatio

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, get, ok, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.http.Http
import fi.oph.koski.json.JsonResources.readResource
import fi.oph.koski.organisaatio.MockOrganisaatioRepository.hierarchyResourcename
import fi.oph.koski.organisaatio.MockOrganisaatiot.helsinginKaupunki
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest._

class RemoteOrganisaatioRepositorySpec extends FreeSpec with Matchers with BeforeAndAfterAll {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues
  implicit val cacheManager = GlobalCacheManager

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9877))
  private val orgRepository = new RemoteOrganisaatioRepository(Http("http://localhost:9877", "organisaatiopalvelu"), KoskiApplicationForTests.koodistoViitePalvelu)

  "RemoteOrganisaatioRepository" - {
    "hakee koulutustoimijan organisaatiohierarkian" in {
      val hierarkia = orgRepository.getOrganisaatioHierarkia(helsinginKaupunki.oid)
      hierarkia should be(MockOrganisaatioRepository.getOrganisaatioHierarkia(helsinginKaupunki.oid))
      hierarkia.map(_.varhaiskasvatuksenJärjestäjä) should equal(Some(true))
    }

    "hakee oppilaitoksen organisaatiohierarkian" in {
      val hierarkia = orgRepository.getOrganisaatioHierarkia(MockOrganisaatiot.stadinAmmattiopisto.oid)
      hierarkia should be(MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.stadinAmmattiopisto.oid))
      hierarkia.map(_.varhaiskasvatuksenJärjestäjä) should equal(Some(false))
    }

    "hakee kaikki päiväkodit" in {
      val count = readResource("/mockdata/organisaatio/varhaiskasvatustoimipisteet.json").extract[Count].numHits
      orgRepository.findAllVarhaiskasvatusToimipisteet.size should equal(count)
    }
  }

  override def beforeAll {
    wireMockServer.start()
    mockEndpoints
  }

  override def afterAll: Unit = wireMockServer.stop()

  private def mockEndpoints = {
    wireMockServer.stubFor(
      get(urlPathEqualTo("/organisaatio-service/rest/organisaatio/v2/hierarkia/hae"))
        .willReturn(ok(write(readResource(hierarchyResourcename(helsinginKaupunki.oid))))))

    wireMockServer.stubFor(
      get(urlPathEqualTo("/organisaatio-service/rest/organisaatio/v4/hae"))
        .willReturn(ok(write(readResource("/mockdata/organisaatio/varhaiskasvatustoimipisteet.json"))))
    )
  }
}

case class Count(numHits: Int)

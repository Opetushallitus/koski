package fi.oph.koski.organisaatio

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{get, ok, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.http.Http
import fi.oph.koski.json.JsonResources.readResource
import fi.oph.koski.organisaatio.MockOrganisaatioRepository.hierarchyResourcename
import fi.oph.koski.organisaatio.MockOrganisaatiot.helsinginKaupunki
import fi.oph.koski.organisaatio.Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RemoteOrganisaatioRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues
  implicit val cacheManager = GlobalCacheManager

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
      val organisaatioHierarkia = organisaatioHierarkiaJson.extract[OrganisaatioHakuTulos].organisaatiot.map(MockOrganisaatioRepository.convertOrganisaatio)
      val päiväkotiCount = OrganisaatioHierarkia.flatten(organisaatioHierarkia).count(_.organisaatiotyypit.contains(VARHAISKASVATUKSEN_TOIMIPAIKKA))
      orgRepository.findAllVarhaiskasvatusToimipisteet.count(_.varhaiskasvatuksenOrganisaatioTyyppi) should equal(päiväkotiCount)
    }

    "hakee varhaiskasvatuksen toimipisteitä jotka eivät ole päiväkoteja" in {
      val organisaatioHierarkia = organisaatioHierarkiaJson.extract[OrganisaatioHakuTulos].organisaatiot.map(MockOrganisaatioRepository.convertOrganisaatio)
      val muuKuinPäiväkotiCount = OrganisaatioHierarkia.flatten(organisaatioHierarkia).count(o =>
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskouluasteenErityiskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.perusJaLukioasteenKoulut)
      )
      orgRepository.findAllVarhaiskasvatusToimipisteet.count(o => !o.varhaiskasvatuksenOrganisaatioTyyppi) should equal(muuKuinPäiväkotiCount)
    }
  }

  override protected def beforeAll {
    super.beforeAll()
    wireMockServer.start()
    mockEndpoints
  }

  override protected def afterAll {
    wireMockServer.stop()
    super.afterAll()
  }

  private def mockEndpoints = {
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/organisaatio-service/rest/organisaatio/v4/${Opetushallitus.organisaatioOid}/jalkelaiset"))
        .willReturn(ok(write(organisaatioHierarkiaJson))))
  }
}

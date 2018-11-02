package fi.oph.koski.henkilo

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.http.KoskiErrorCategory.badRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest.{BeforeAndAfterAll, EitherValues, FreeSpec, Matchers, OptionValues}

class OppijanumeroRekisteriClientSpec extends FreeSpec with Matchers with EitherValues with OptionValues with BeforeAndAfterAll {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9876"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
    """.stripMargin)

  private val mockClient = OppijanumeroRekisteriClient(config)

  private val hetu = "120456-ABCD"
  private val oid = "1.2.246.562.24.99999999123"
  private val organisaatioOid = "1.2.246.562.10.85149969462"

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9876))

  private val defaultOppijaHenkilöResponse = Map(
    "oidHenkilo" -> oid,
    "sukunimi" -> "Mallikas",
    "etunimet" -> "Mikko Alfons",
    "kutsumanimi" -> "Mikko",
    "hetu" -> hetu,
    "syntymaaika" -> "1956-04-12",
    "modified" -> 1541163791)

  private val expectedKäyttäjäHenkilö = KäyttäjäHenkilö(
    oid,
    "Mallikas",
    "Mikko Alfons",
    None)

  private val expectedOppijaHenkilö = OppijaHenkilö(
    oid,
    "Mallikas",
    "Mikko Alfons",
    "Mikko",
    Some(hetu),
    Some(LocalDate.parse("1956-04-12")),
    None,
    None,
    1541163791,
    false,
    List.empty[String])

  override def beforeAll {
    wireMockServer.start()
    mockEndpoints()
    super.beforeAll()
  }

  override def afterAll {
    wireMockServer.stop()
    super.afterAll()
  }

  "OppijanumeroRekisteriClient" - {

    "perustaa uuden oppijahenkilön" in {
      val result = mockClient.findOrCreate(UusiOppijaHenkilö(Some(hetu), "Mallikas", "Mikko Alfons", "Mikko")).run
      result.right.value should equal(expectedOppijaHenkilö)
    }

    "tunnistaa 400-vastauksen uutta oppijahenkilöä perustaessa" in {
      val result = mockClient.findOrCreate(UusiOppijaHenkilö(Some(hetu), "", "Mikko Alfons", "Mikko")).run
      result.left.value.statusCode should equal(400)
    }

    "palauttaa käyttäjähenkilön tiedot oid:n perusteella" in {
      val result = mockClient.findKäyttäjäByOid(oid).run
      result.value should equal(expectedKäyttäjäHenkilö)
    }

    "palauttaa sähköpostiosoitteet oid:n ja käyttöoikeusryhmän perusteella" in {
      val result = mockClient.findSähköpostit(organisaatioOid, "KOSKI").run
      result should contain only ("mikko.mallikas@suomi.fi")
    }

    "palauttaa oppijahenkilön tiedot oid:n perusteella" in {
      val result = mockClient.findOppijaByOid(oid).run
      result.value should equal(expectedOppijaHenkilö)
    }

    "palauttaa oppijahenkilön master-tiedot oid:n perusteella" in {
      val result = mockClient.findMasterOppija(oid).run
      result.value should equal(expectedOppijaHenkilö)
    }

    "palauttaa oppijahenkilön tiedot oid-listan perusteella" in {
      val result = mockClient.findOppijatNoSlaveOids(List(oid)).run
      result should contain only (expectedOppijaHenkilö)
    }

    "palauttaa muuttuneiden oppijahenkilön oid:t" in {
      val result = mockClient.findChangedOppijaOids(1541163791, 0, 1).run
      result should contain only (oid)
    }

    "palauttaa oppijahenkilön tiedot hetun perusteella" in {
      val result = mockClient.findOppijaByHetu(hetu).run
      result.value should equal(expectedOppijaHenkilö)
    }

    "palauttaa oppijahenkilön tiedot hetu-listan perusteella" in {
      val result = mockClient.findOppijatByHetusNoSlaveOids(List(hetu)).run
      result should contain only (expectedOppijaHenkilö)
    }

    "kutsumanimen puuttuessa käyttää ensimmäistä etunimeä kutsumanimenä" in {
      mockEndpoints(defaultOppijaHenkilöResponse + ("kutsumanimi" -> None))
      val result = mockClient.findOppijaByHetu(hetu).run
      result.value should equal(expectedOppijaHenkilö)
    }

  }

  def mockEndpoints(oppijaHenkilöResponseData: Map[String, Any] = defaultOppijaHenkilöResponse) = {
    val hetuUrl = s"/oppijanumerorekisteri-service/henkilo/hetu=${hetu}"
    val hetusUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList"
    val oidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}"
    val masterOidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/master"
    val changedSinceUrl = "/oppijanumerorekisteri-service/s2s/changedSince/([1-9]*)"
    val perustiedotUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList"
    val yhteystiedotUrl = "/oppijanumerorekisteri-service/s2s/henkilo/yhteystiedot"
    val slaveOidsUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/slaves"
    val uusiHenkiloUrl = "/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto"

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(uusiHenkiloUrl))
        .willReturn(status(400)))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(uusiHenkiloUrl))
        .withRequestBody(matchingJsonPath("$[?(@.sukunimi == 'Mallikas')]"))
        .willReturn(ok().withBody(write(oppijaHenkilöResponseData))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(hetuUrl))
        .willReturn(ok().withBody(write(oppijaHenkilöResponseData))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(hetusUrl))
        .willReturn(ok().withBody(write(List(oppijaHenkilöResponseData)))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(oidUrl))
        .willReturn(ok().withBody(write(Map("oidHenkilo" -> oid, "sukunimi" -> "Mallikas", "etunimet" -> "Mikko Alfons")))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(masterOidUrl))
        .willReturn(ok().withBody(write(oppijaHenkilöResponseData))))

    wireMockServer.stubFor(
      WireMock.get(urlPathMatching(changedSinceUrl))
        .willReturn(ok().withBody(write(List(oid)))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(perustiedotUrl))
        .willReturn(ok().withBody(write(List(oppijaHenkilöResponseData)))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(yhteystiedotUrl))
        .willReturn(ok().withBody(write(List(Map(
          "yhteystiedotRyhma" -> List(
            Map("yhteystieto" -> List(
              Map("yhteystietoTyyppi" -> "YHTEYSTIETO_MUU", "yhteystietoArvo" -> "@mmallikas"),
              Map("yhteystietoTyyppi" -> "YHTEYSTIETO_SAHKOPOSTI", "yhteystietoArvo" -> "mikko.mallikas@suomi.fi")
            )))))))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(slaveOidsUrl))
        .willReturn(ok().withBody(write(List.empty[String]))))
  }
}

package fi.oph.koski.henkilo

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest._

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
  private val vanhaHetu = "120456-DCBA"
  private val oid = "1.2.246.562.24.99999999123"
  private val slaveOid = "1.2.246.562.24.99999999124"
  private val organisaatioOid = "1.2.246.562.10.85149969462"

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9876))

  private val defaultPerustietoResponse = Map(
    "oidHenkilo" -> oid,
    "sukunimi" -> "Mallikas",
    "etunimet" -> "Mikko Alfons",
    "kutsumanimi" -> "Mikko",
    "hetu" -> hetu,
    "syntymaaika" -> "1956-04-12",
    "modified" -> 1543411774579l,
    "externalIds" -> null,
    "identifications" -> null,
    "turvakielto" -> false,
    "aidinkieli" -> null,
    "asiointiKieli" -> null,
    "kansalaisuus" -> null,
    "sukupuoli" -> "1",
    "palveluasiayhteys" -> null,
    "henkiloTyyppi" -> "OPPIJA"
  )

  private val defaultHenkilöResponse = Map(
    "id" -> 363003,
    "sukunimi" -> "Mallikas",
    "etunimet" -> "Mikko Alfons",
    "kutsumanimi" -> "Mikko",
    "kuolinpaiva" -> null,
    "syntymaaika" -> "1956-04-12",
    "hetu" -> hetu,
    "kaikkiHetut" -> List(
      hetu,
      vanhaHetu
    ),
    "oidHenkilo" -> oid,
    "oppijanumero" -> oid,
    "sukupuoli" -> "1",
    "kotikunta" -> null,
    "turvakielto" -> null,
    "eiSuomalaistaHetua" -> false,
    "passivoitu" -> false,
    "yksiloity" -> false,
    "yksiloityVTJ" -> true,
    "yksilointiYritetty" -> false,
    "duplicate" -> false,
    "created" -> 1543411645075l,
    "modified" -> 1543411774579l,
    "vtjsynced" -> null,
    "kasittelijaOid" -> null,
    "asiointiKieli" -> null,
    "aidinkieli" -> null,
    "kielisyys" -> null,
    "kansalaisuus" -> null,
    "yhteystiedotRyhma" -> null,
    "henkiloTyyppi" -> "OPPIJA"
  )

  private val masterHenkilöResponse = Map(
    slaveOid -> Map(
      "oidHenkilo" -> oid,
      "hetu" -> hetu,
      "etunimet" -> "Mikko Alfons",
      "kutsumanimi" -> "Mikko",
      "sukunimi" -> "Mallikas",
      "syntymaaika" -> "1956-04-12",
      "modified" -> 1543411774579l
    ),
    oid -> Map(
      "oidHenkilo" -> oid,
      "hetu" -> hetu,
      "kaikkiHetut" -> List(vanhaHetu),
      "etunimet" -> "Mikko Alfons",
      "kutsumanimi" -> "Mikko",
      "sukunimi" -> "Mallikas",
      "syntymaaika" -> "1956-04-12",
      "modified" -> 1543411774579l
    )
  )

  private val expectedKäyttäjäHenkilö = KäyttäjäHenkilö(
    oid,
    "Mallikas",
    "Mikko Alfons",
    None)

  private val expectedOppijaHenkilö = OppijaHenkilö(
    oid = oid,
    sukunimi = "Mallikas",
    etunimet = "Mikko Alfons",
    kutsumanimi = "Mikko",
    hetu = Some(hetu),
    syntymäaika = Some(LocalDate.parse("1956-04-12")),
    äidinkieli = None,
    kansalaisuus = None,
    modified = 1543411774579l,
    turvakielto = false,
    linkitetytOidit = Nil
  )

  private val expectedOppijaHenkilöHetuMuuttunut = expectedOppijaHenkilö.copy(vanhatHetut = List(vanhaHetu))

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
      val result = mockClient.findTyöSähköpostiosoitteet(organisaatioOid, "KOSKI").run
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
      result.value should equal(expectedOppijaHenkilöHetuMuuttunut)
    }

    "palauttaa oppijahenkilön tiedot hetu-listan perusteella" in {
      val result = mockClient.findOppijatByHetusNoSlaveOids(List(hetu)).run
      result should contain only (expectedOppijaHenkilö)
    }

    "kutsumanimen puuttuessa käyttää ensimmäistä etunimeä kutsumanimenä" in {
      mockEndpoints(defaultPerustietoResponse + ("kutsumanimi" -> None))
      val result = mockClient.findOppijaByHetu(hetu).run
      result.value should equal(expectedOppijaHenkilöHetuMuuttunut)
    }

    "palauttaa listan slave oideista" in {
      mockEndpoints(slaveOidsResponseData = List(Map("oidHenkilo" -> slaveOid, "etunimet" -> "Paavo", "sukunimi" -> "Pesusieni", "modified" -> 1541163791)))
      val result = mockClient.findSlaveOids(oid).run
      result should contain only slaveOid
    }

    "palauttaa listan slave oideista vaikka datasta puuttuu etunimet ja sukunimi" in {
      mockEndpoints(slaveOidsResponseData = List(Map("oidHenkilo" -> slaveOid, "etunimet" -> None, "sukunimi" -> None, "modified" -> 1541163791)))
      val result = mockClient.findSlaveOids(oid).run
      result should contain only slaveOid
    }
  }

  def mockEndpoints(perustietoResponseData: Map[String, Any] = defaultPerustietoResponse, slaveOidsResponseData: List[Map[String, Any]] = List.empty) = {
    val hetuUrl = s"/oppijanumerorekisteri-service/henkilo/hetu=${hetu}"
    val hetusUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList"
    val oidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}"
    val masterOidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/master"
    val changedSinceUrl = "/oppijanumerorekisteri-service/s2s/changedSince/([1-9]*)"
    val perustiedotUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList"
    val yhteystiedotUrl = "/oppijanumerorekisteri-service/s2s/henkilo/yhteystiedot"
    val slaveOidsUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/slaves"
    val uusiHenkiloUrl = "/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto"
    val masterHenkilötUrl = "/oppijanumerorekisteri-service/henkilo/masterHenkilosByOidList"

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(uusiHenkiloUrl))
        .willReturn(status(400)))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(uusiHenkiloUrl))
        .withRequestBody(matchingJsonPath("$[?(@.sukunimi == 'Mallikas')]"))
        .willReturn(ok().withBody(write(perustietoResponseData))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(hetuUrl))
        .willReturn(ok().withBody(write(defaultHenkilöResponse))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(hetusUrl))
        .willReturn(ok().withBody(write(List(perustietoResponseData)))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(oidUrl))
        .willReturn(ok().withBody(write(Map("oidHenkilo" -> oid, "sukunimi" -> "Mallikas", "etunimet" -> "Mikko Alfons")))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(masterOidUrl))
        .willReturn(ok().withBody(write(perustietoResponseData))))

    wireMockServer.stubFor(
      WireMock.get(urlPathMatching(changedSinceUrl))
        .willReturn(ok().withBody(write(List(oid)))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(perustiedotUrl))
        .willReturn(ok().withBody(write(List(perustietoResponseData)))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(masterHenkilötUrl))
        .willReturn(ok().withBody(write(masterHenkilöResponse))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(yhteystiedotUrl))
        .willReturn(ok().withBody(write(List(Map(
          "yhteystiedotRyhma" -> List(
            Map("ryhmaKuvaus" -> "yhteystietotyyppi2", "yhteystieto" -> List(
              Map("yhteystietoTyyppi" -> "YHTEYSTIETO_MUU", "yhteystietoArvo" -> "@mmallikas"),
              Map("yhteystietoTyyppi" -> "YHTEYSTIETO_SAHKOPOSTI", "yhteystietoArvo" -> "mikko.mallikas@suomi.fi")
            )),
            Map("ryhmaKuvaus" -> "yhteystietotyyppi8", "yhteystieto" -> List(
              Map("yhteystietoTyyppi" -> "YHTEYSTIETO_SAHKOPOSTI", "yhteystietoArvo" -> "mikko.mallikas@kotiosoite.fi")
            )))))))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(slaveOidsUrl))
        .willReturn(ok().withBody(write(slaveOidsResponseData))))
  }
}

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

  private val expectedKäyttäjäHenkilö = KäyttäjäHenkilö(
    oid,
    "Mallikas",
    "Mikko Alfons",
    None)

  private val expectedLaajatOppijaHenkilötiedot = LaajatOppijaHenkilöTiedot(
    oid = oid,
    sukunimi = "Mallikas",
    etunimet = "Mikko Alfons",
    kutsumanimi = "Mikko",
    hetu = Some(hetu),
    sukupuoli = Some("1"),
    syntymäaika = Some(LocalDate.parse("1956-04-12")),
    äidinkieli = None,
    kansalaisuus = None,
    modified = 1543411774579l,
    turvakielto = false,
    linkitetytOidit = Nil,
    yksilöity = true,
    vanhatHetut = List(vanhaHetu)
  )

  private val expectedSuppeatOppijaHenkilötiedot = SuppeatOppijaHenkilöTiedot(
    oid = oid,
    sukunimi = "Mallikas",
    etunimet = "Mikko Alfons",
    kutsumanimi = "Mikko",
    hetu = Some(hetu),
    sukupuoli = Some("1"),
    syntymäaika = Some(LocalDate.parse("1956-04-12")),
    äidinkieli = None,
    kansalaisuus = None,
    modified = 1543411774579l,
    turvakielto = false,
    linkitetytOidit = Nil
  )

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
      result.right.value should equal(expectedSuppeatOppijaHenkilötiedot)
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
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön master-tiedot oid:n perusteella" in {
      val result = mockClient.findMasterOppija(oid).run
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön tiedot oid-listan perusteella" in {
      val result = mockClient.findOppijatNoSlaveOids(List(oid)).run
      result should contain only expectedSuppeatOppijaHenkilötiedot
    }

    "palauttaa muuttuneiden oppijahenkilön oid:t" in {
      val result = mockClient.findChangedOppijaOids(1541163791, 0, 1).run
      result should contain only (oid)
    }

    "palauttaa oppijahenkilön tiedot hetun perusteella" in {
      val result = mockClient.findOppijaByHetu(hetu).run
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön tiedot hetu-listan perusteella" in {
      val result = mockClient.findOppijatByHetusNoSlaveOids(List(hetu)).run
      result should contain only expectedSuppeatOppijaHenkilötiedot
    }

    "kutsumanimen puuttuessa käyttää ensimmäistä etunimeä kutsumanimenä" in {
      mockEndpoints(defaultHenkilöResponse + ("kutsumanimi" -> None))
      val result = mockClient.findOppijaByHetu(hetu).run
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
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

    "palauttaa annetun kotikunnan jos kotikunta on asetettu" in {
      mockEndpoints(defaultHenkilöResponse + ("kotikunta" -> "091"))
      val result = mockClient.findMasterOppija(oid).run
      result.value.kotikunta should equal(Some("091"))
    }

    "palauttaa kotikuntana None jos kotikunta on null" in {
      mockEndpoints(defaultHenkilöResponse + ("kotikunta" -> None))
      val result = mockClient.findMasterOppija(oid).run
      result.value.kotikunta should equal(None)
    }

    "palauttaa kotikuntana None jos kotikunta ei ole määritelty" in {
      mockEndpoints(defaultHenkilöResponse - "kotikunta")
      val result = mockClient.findMasterOppija(oid).run
      result.value.kotikunta should equal(None)
    }

    "palauttaa yksilöintitiedon mikäli asetettu" in {
      mockEndpoints(defaultHenkilöResponse + ("yksiloity" -> true, "yksiloityVTJ" -> true))
      val result = mockClient.findMasterOppija(oid).run
      result.value.yksilöity should equal(true)
    }

    "palauttaa yksilöity false jos yksilöintitieto on null" in {
      mockEndpoints(defaultHenkilöResponse + ("yksiloity" -> None, "yksiloityVTJ" -> None))
      val result = mockClient.findMasterOppija(oid).run
      result.value.yksilöity should equal(false)
    }

    "palauttaa yksilöity false jos yksilöintitietoa ei ole määritelty" in {
      mockEndpoints(defaultHenkilöResponse - ("yksiloity", "yksiloityVTJ"))
      val result = mockClient.findMasterOppija(oid).run
      result.value.yksilöity should equal(false)
    }
  }

  def mockEndpoints(henkilöResponse: Map[String, Any] = defaultHenkilöResponse, slaveOidsResponseData: List[Map[String, Any]] = List.empty) = {
    val hetuUrl = s"/oppijanumerorekisteri-service/henkilo/hetu=${hetu}"
    val hetuPerustietoUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList"
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
        .willReturn(ok().withBody(write(defaultPerustietoResponse))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(hetuUrl))
        .willReturn(ok().withBody(write(henkilöResponse))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(hetuPerustietoUrl))
        .willReturn(ok().withBody(write(List(defaultPerustietoResponse)))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(oidUrl))
        .willReturn(ok().withBody(write(henkilöResponse))))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(masterOidUrl))
        .willReturn(ok().withBody(write(henkilöResponse))))

    wireMockServer.stubFor(
      WireMock.get(urlPathMatching(changedSinceUrl))
        .willReturn(ok().withBody(write(List(oid)))))

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(perustiedotUrl))
        .willReturn(ok().withBody(write(List(defaultPerustietoResponse)))))

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

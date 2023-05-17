package fi.oph.koski.henkilo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import fi.oph.koski.TestEnvironment
import fi.oph.koski.http.Http
import fi.oph.koski.schema.Koodistokoodiviite
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class OppijanumeroRekisteriClientSpec
  extends AnyFreeSpec
    with TestEnvironment
    with Matchers
    with EitherValues
    with OptionValues
    with BeforeAndAfterAll {

  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9876"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
      |opintopolku.environment="mock"
    """.stripMargin)

  private val mockClient = OppijanumeroRekisteriClient(config)

  private val hetu = "120456-ABCD"
  private val vanhaHetu = "120456-DCBA"
  private val oid = "1.2.246.562.24.99999999123"
  private val slaveOid = "1.2.246.562.24.99999999124"
  private val deserializationTestOid = "1.2.246.562.24.76912370865"
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
    "kotikunta" -> Some("179"),
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
    "henkiloTyyppi" -> "OPPIJA",
    "yhteystiedotRyhma" -> List(
      Map(
        "id" -> 163351041,
        "ryhmaKuvaus" -> "yhteystietotyyppi13",
        "ryhmaAlkuperaTieto" -> "alkupera7",
        "readOnly" -> false,
        "yhteystieto" -> List(
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_PUHELINNUMERO",
            "yhteystietoArvo" -> "0401122334"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_MATKAPUHELINNUMERO",
            "yhteystietoArvo" -> "0401122334"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_SAHKOPOSTI",
            "yhteystietoArvo" -> "esimerkki@gmail.com"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_KUNTA",
            "yhteystietoArvo" -> "Helsinki"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_KATUOSOITE",
            "yhteystietoArvo" -> "Esimerkkitie 10"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_KAUPUNKI",
            "yhteystietoArvo" -> "Helsinki"
          ),
          Map(
            "yhteystietoTyyppi" -> "YHTEYSTIETO_POSTINUMERO",
            "yhteystietoArvo" -> "00300"
          )
        )
      )
    )
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
    vanhatHetut = List(vanhaHetu),
    yhteystiedot = List(Yhteystiedot(
      alkuperä = Koodistokoodiviite("alkupera7", "yhteystietojenalkupera"),
      tyyppi = Koodistokoodiviite("yhteystietotyyppi13", "yhteystietotyypit"),
      sähköposti = Some("esimerkki@gmail.com"),
      puhelinnumero = Some("0401122334"),
      matkapuhelinnumero = Some("0401122334"),
      katuosoite = Some("Esimerkkitie 10"),
      kunta = Some("Helsinki"),
      postinumero = Some("00300"),
      kaupunki = Some("Helsinki"),
      maa = None,
    ))
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

  private val realLaajaOppijaHenkilötResponse =
    """{
          "oidHenkilo": "1.2.246.562.24.76912370865",
          "hetu": "200220A924F",
          "kaikkiHetut": [],
          "passivoitu": false,
          "etunimet": "Aamu Annika",
          "kutsumanimi": "Aamu",
          "sukunimi": "Aalto",
          "aidinkieli": null,
          "asiointiKieli": null,
          "kansalaisuus": [],
          "kasittelijaOid": "1.2.246.562.24.53996747039",
          "syntymaaika": "2020-02-20",
          "sukupuoli": "2",
          "kotikunta": null,
          "oppijanumero": null,
          "turvakielto": false,
          "eiSuomalaistaHetua": false,
          "yksiloity": false,
          "yksiloityVTJ": false,
          "yksilointiYritetty": true,
          "duplicate": false,
          "created": 1604324426993,
          "modified": 1604324808996,
          "vtjsynced": null,
          "yhteystiedotRyhma": [
            {
              "id": 163351041,
              "ryhmaKuvaus": "yhteystietotyyppi13",
              "ryhmaAlkuperaTieto": "alkupera7",
              "readOnly": false,
              "yhteystieto": [
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_SAHKOPOSTI",
                  "yhteystietoArvo": "esimerkki@gmail.com"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_KATUOSOITE",
                  "yhteystietoArvo": "Esimerkkitie 10"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_PUHELINNUMERO",
                  "yhteystietoArvo": "0401122334"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_POSTINUMERO",
                  "yhteystietoArvo": "00300"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_MATKAPUHELINNUMERO",
                  "yhteystietoArvo": "0401122334"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_KAUPUNKI",
                  "yhteystietoArvo": "Helsinki"
                },
                {
                  "yhteystietoTyyppi": "YHTEYSTIETO_KUNTA",
                  "yhteystietoArvo": "Helsinki"
                }
              ]
            }
          ],
          "yksilointivirheet": [
            {
              "yksilointivirheTila": "HETU_EI_OIKEA",
              "uudelleenyritysAikaleima": null
            }
          ],
          "henkiloTyyppi": "OPPIJA",
          "kielisyys": []
        }"""

  override protected def beforeAll {
    super.beforeAll()
    wireMockServer.start()
    mockEndpoints()
  }

  override protected def afterAll {
    wireMockServer.stop()
    super.afterAll()
  }

  "OppijanumeroRekisteriClient" - {

    "perustaa uuden oppijahenkilön" in {
      val result = Http.runIO(mockClient.findOrCreate(UusiOppijaHenkilö(Some(hetu), "Mallikas", "Mikko Alfons", "Mikko")))
      result.value should equal(expectedSuppeatOppijaHenkilötiedot)
    }

    "tunnistaa 400-vastauksen uutta oppijahenkilöä perustaessa" in {
      val result = Http.runIO(mockClient.findOrCreate(UusiOppijaHenkilö(Some(hetu), "", "Mikko Alfons", "Mikko")))
      result.left.value.statusCode should equal(400)
    }

    "palauttaa käyttäjähenkilön tiedot oid:n perusteella" in {
      val result = Http.runIO(mockClient.findKäyttäjäByOid(oid))
      result.value should equal(expectedKäyttäjäHenkilö)
    }

    "palauttaa oppijahenkilön tiedot oid:n perusteella" in {
      val result = Http.runIO(mockClient.findOppijaByOid(oid))
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön master-tiedot oid:n perusteella" in {
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön tiedot oid-listan perusteella" in {
      val result = Http.runIO(mockClient.findOppijatNoSlaveOids(List(oid)))
      result should contain only expectedSuppeatOppijaHenkilötiedot
    }

    "palauttaa muuttuneiden oppijahenkilön oid:t" in {
      val result = Http.runIO(mockClient.findChangedOppijaOids(1541163791, 0, 1))
      result should contain only (oid)
    }

    "palauttaa sivutetut oppijat syntymäajan ja kotikunnan perusteella" in {
      // Helsinki 091
      val result = Http.runIO(mockClient.findByVarhaisinSyntymäaikaAndKotikunta("2005-01-01", "091", 1))
      result.results should be(Seq.empty)
    }

    "palauttaa oppijahenkilön tiedot hetun perusteella" in {
      val result = Http.runIO(mockClient.findOppijaByHetu(hetu))
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa oppijahenkilön tiedot hetu-listan perusteella" in {
      val result = Http.runIO(mockClient.findOppijatByHetusNoSlaveOids(List(hetu)))
      result should contain only expectedSuppeatOppijaHenkilötiedot
    }

    "kutsumanimen puuttuessa käyttää ensimmäistä etunimeä kutsumanimenä" in {
      mockEndpoints(defaultHenkilöResponse + ("kutsumanimi" -> None))
      val result = Http.runIO(mockClient.findOppijaByHetu(hetu))
      result.value should equal(expectedLaajatOppijaHenkilötiedot)
    }

    "palauttaa listan slave oideista" in {
      mockEndpoints(slaveOidsResponseData = List(Map("oidHenkilo" -> slaveOid, "etunimet" -> "Paavo", "sukunimi" -> "Pesusieni", "modified" -> 1541163791)))
      val result = Http.runIO(mockClient.findSlaveOids(oid))
      result should contain only slaveOid
    }

    "palauttaa listan slave oideista vaikka datasta puuttuu etunimet ja sukunimi" in {
      mockEndpoints(slaveOidsResponseData = List(Map("oidHenkilo" -> slaveOid, "etunimet" -> None, "sukunimi" -> None, "modified" -> 1541163791)))
      val result = Http.runIO(mockClient.findSlaveOids(oid))
      result should contain only slaveOid
    }

    "palauttaa annetun kotikunnan jos kotikunta on asetettu" in {
      mockEndpoints(defaultHenkilöResponse + ("kotikunta" -> "091"))
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.kotikunta should equal(Some("091"))
    }

    "palauttaa kotikuntana None jos kotikunta on null" in {
      mockEndpoints(defaultHenkilöResponse + ("kotikunta" -> None))
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.kotikunta should equal(None)
    }

    "palauttaa kotikuntana None jos kotikunta ei ole määritelty" in {
      mockEndpoints(defaultHenkilöResponse - "kotikunta")
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.kotikunta should equal(None)
    }

    "palauttaa yksilöintitiedon mikäli asetettu" in {
      mockEndpoints(defaultHenkilöResponse + ("yksiloity" -> true, "yksiloityVTJ" -> true))
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.yksilöity should equal(true)
    }

    "palauttaa yksilöity false jos yksilöintitieto on null" in {
      mockEndpoints(defaultHenkilöResponse + ("yksiloity" -> None, "yksiloityVTJ" -> None))
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.yksilöity should equal(false)
    }

    "palauttaa yksilöity false jos yksilöintitietoa ei ole määritelty" in {
      mockEndpoints(defaultHenkilöResponse - ("yksiloity", "yksiloityVTJ"))
      val result = Http.runIO(mockClient.findMasterOppija(oid))
      result.value.yksilöity should equal(false)
    }

    "deserialisoi oikean JSON-merkkijonon" in {
      mockEndpoints()
      val result = Http.runIO(mockClient.findOppijaByOid(deserializationTestOid))
      result.value.yhteystiedot should equal(expectedLaajatOppijaHenkilötiedot.yhteystiedot)
    }
  }

  def mockEndpoints(henkilöResponse: Map[String, Any] = defaultHenkilöResponse, slaveOidsResponseData: List[Map[String, Any]] = List.empty) = {
    val hetuUrl = s"/oppijanumerorekisteri-service/henkilo/hetu=${hetu}"
    val hetuPerustietoUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloHetuList"
    val oidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}"
    val masterOidUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/master"
    val changedSinceUrl = "/oppijanumerorekisteri-service/s2s/changedSince/([1-9]*)"
    val listUrl = "/oppijanumerorekisteri-service/s2s/henkilo/list/091/2005-01-01"
    val perustiedotUrl = "/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList"
    val yhteystiedotUrl = "/oppijanumerorekisteri-service/s2s/henkilo/yhteystiedot"
    val slaveOidsUrl = s"/oppijanumerorekisteri-service/henkilo/${oid}/slaves"
    val uusiHenkiloUrl = "/oppijanumerorekisteri-service/s2s/findOrCreateHenkiloPerustieto"
    val deserializationOidUrl = s"/oppijanumerorekisteri-service/henkilo/${deserializationTestOid}"
    val deserializationSlaveOidsUrl = s"/oppijanumerorekisteri-service/henkilo/${deserializationTestOid}/slaves"

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
      WireMock.get(urlPathMatching(listUrl))
        .willReturn(ok().withBody(write(
          OppijaNumerorekisteriKuntarouhintatiedot(
            first = true,
            last = true,
            number = 1,
            numberOfElements = 0,
            size = 10,
            results = Seq.empty
          )
        )))
    )

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

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(deserializationOidUrl))
        .willReturn(ok().withBody(realLaajaOppijaHenkilötResponse)))

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(deserializationSlaveOidsUrl))
        .willReturn(ok().withBody(write(slaveOidsResponseData))))
  }
}

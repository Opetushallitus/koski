package fi.oph.koski.valpas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.Fault
import com.typesafe.config.ConfigFactory
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.schema.BlankableLocalizedString
import fi.oph.koski.valpas.hakukooste.{Hakukooste, Hakutoive, HakukoosteExampleData, ValpasHakukoosteService}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilöLaajatTiedot, ValpasOppivelvollinenOppijaLaajatTiedot}
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, EitherValues}

import java.time.{LocalDate, LocalDateTime}

class OvaraHakukoosteServiceSpec extends ValpasTestBase with Matchers with EitherValues with BeforeAndAfterAll {
  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9876"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
      |valpas.hakukoosteEnabled = true
      |valpas.hakukoosteTimeoutSeconds = 2
      |valpas.hakukoosteService = "ovara"
    """.stripMargin)

  private val mockClient = ValpasHakukoosteService(KoskiApplicationForTests, Some(config))

  private val masterHenkilö = ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster
  private val slaveHenkilö = ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9876))

  private val ovaraHakukoosteUrl = "/ovara-backend/api/valpas"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "OvaraHakukoosteService" - {
    "käsittelee virhetilanteen kun ovara ei vastaa" in {
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .willReturn(WireMock.status(500)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "käsittelee virhetilanteen kun vastaus ei vastaa schemaa" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockGetResponseForOids(queryOids, hakutapaKoodiarvo = "virheellinen_koodiarvo")

      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(502)
      result.errorString.get should startWith("Ovaran palauttama hakukoostetieto oli viallinen")
    }

    "käsittelee virhetilanteen kun JSON-vastauksen kenttärakenne on virheellinen" in {
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .willReturn(WireMock.ok().withBody("""[{"tuntematon_kentta": "foo"}]""")))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(502)
      result.errorString.get should startWith("Ovaran palauttama hakukoostetieto oli viallinen")
    }

    "toimii kun vastaus katkeaa kesken" in {
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "toimii kun vastauksessa kestää liian kauan" in {
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .willReturn(aResponse().withFixedDelay(10000)))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = false, "test").left.value
      result.statusCode should equal(503)
      result.errorString.get should startWith("Hakukoosteita ei juuri nyt saada haettua")
    }

    "toimii kun vastaus on tyhjä (GET)" in {
      val queryOids = Set("tuntematon-oid")
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .withQueryParam("ovara_oppijanumero", WireMock.equalTo("tuntematon-oid"))
          .willReturn(WireMock.ok().withBody("[]")))

      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value
      result.map(_.oppijaOid) should equal(List.empty)
    }

    "palauttaa hakukoostetiedot kun oid löytyy (GET)" in {
      val queryOids = Set(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
      mockGetResponseForOids(queryOids)

      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value
      result.map(_.oppijaOid) should equal(queryOids.toList)
    }

    "käyttää POST-metodia usealle oidille" in {
      val queryOids = Set(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
        ValpasMockOppijat.turvakieltoOppija.oid,
      )
      mockPostResponseForOids(queryOids)

      val result = mockClient.getHakukoosteet(queryOids, ainoastaanAktiivisetHaut = false, "test").value
      result.map(_.oppijaOid).toSet should equal(queryOids)
    }

    "välittää ainoastaanAktiivisetHaut-parametrin GET-kutsussa" in {
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .withQueryParam("ainoastaanAktiivisetHaut", WireMock.equalTo("true"))
          .willReturn(WireMock.ok().withBody("[]")))

      val result = mockClient.getHakukoosteet(Set("asdf"), ainoastaanAktiivisetHaut = true, "test").value
      result should be(empty)
    }

    "välittää ainoastaanAktiivisetHaut-parametrin POST-kutsussa" in {
      wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .withQueryParam("ainoastaanAktiivisetHaut", WireMock.equalTo("true"))
          .willReturn(WireMock.ok().withBody("[]")))

      val result = mockClient.getHakukoosteet(Set("asdf1", "asdf2"), ainoastaanAktiivisetHaut = true, "test").value
      result should be(empty)
    }

    "osaa serialisoida aidon API-vastauksen" in {
      val oppijaOid = "1.2.246.562.24.10545516007"
      wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
          .withQueryParam("ovara_oppijanumero", WireMock.equalTo(oppijaOid))
          .willReturn(WireMock.ok().withBody(OvaraHakukoosteServiceSpec.realResponse(oppijaOid)))
      )
      val results = mockClient.getHakukoosteet(Set(oppijaOid), ainoastaanAktiivisetHaut = false, "test").value
      results should have length 2
      val result = results.head
      result.oppijaOid should equal(oppijaOid)
      result.hakutapa.koodiarvo should equal("01")
      result.hakuNimi.get("fi") should startWith("Perusopetuksen jälkeisen")
      result.haunAlkamispaivamaara should equal(LocalDateTime.of(2024, 8, 31, 0, 0))
      result.hakemuksenMuokkauksenAikaleima should equal(Some(LocalDateTime.of(2026, 3, 30, 21, 37, 13, 544214000)))
      result.maa.map(_.koodiarvo) should equal(Some("246"))
      result.hakutoiveet should have length 3
      result.hakutoiveet.head.hakukohdeOid should equal("1.2.246.562.20.00000000000000080037")
      result.hakutoiveet.head.harkinnanvaraisuus should be(None)
    }

    "palauttaa hakutilanteet kun ovara palauttaa saman oppija-oidin" in {
      mockGetResponseForOids(Set(masterHenkilö.oid))
      val oppijat = Seq(minimalValpasOppija(masterHenkilö))
      val result = mockClient.fetchHautYhteystiedoilla("test", Seq(masterHenkilö.oid))(oppijat)
      result.head.hakutilanteet should not be empty
      result.head.oppija.henkilö.oid shouldBe masterHenkilö.oid
      result.head.hakutilanteet.head.debugHakukooste.get.oppijaOid shouldBe masterHenkilö.oid
    }

    "yhdistää hakukoosteen oppijaan kun ovara palauttaa slave-oppija-oidin" in {
      mockGetResponseForOids(Set(masterHenkilö.oid), responseOppijaOid = Some(slaveHenkilö.oid))
      val oppijat = Seq(minimalValpasOppija(masterHenkilö))
      val result = mockClient.fetchHautYhteystiedoilla("test", Seq(masterHenkilö.oid))(oppijat)
      result.length shouldBe 1
      result.head.hakutilanteet should not be empty
      result.head.oppija.henkilö.oid shouldBe masterHenkilö.oid
      result.head.hakutilanteet.head.debugHakukooste.get.oppijaOid shouldBe slaveHenkilö.oid
    }

    "palauttaa tyhjät hakutilanteet kun slave-oppija-oidiä ei voida yhdistää masteriin" in {
      mockGetResponseForOids(Set(masterHenkilö.oid), responseOppijaOid = Some("1.2.246.562.24.tuntematon_oid_99999"))
      val oppijat = Seq(minimalValpasOppija(masterHenkilö))
      val result = mockClient.fetchHautYhteystiedoilla("test", Seq(masterHenkilö.oid))(oppijat)
      result.length shouldBe 1
      result.head.hakutilanteet shouldBe empty
    }
  }

  private def minimalValpasOppija(henkilö: LaajatOppijaHenkilöTiedot): ValpasOppivelvollinenOppijaLaajatTiedot =
    ValpasOppivelvollinenOppijaLaajatTiedot(
      henkilö = ValpasHenkilöLaajatTiedot(
        oid = henkilö.oid,
        kaikkiOidit = Set(henkilö.oid),
        hetu = henkilö.hetu,
        syntymäaika = henkilö.syntymäaika,
        etunimet = henkilö.etunimet,
        sukunimi = henkilö.sukunimi,
        kotikunta = None,
        turvakielto = false,
        äidinkieli = None,
      ),
      hakeutumisvalvovatOppilaitokset = Set.empty,
      suorittamisvalvovatOppilaitokset = Set.empty,
      opiskeluoikeudet = Seq.empty,
      oppivelvollisuusVoimassaAsti = LocalDate.of(2025, 1, 1),
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = LocalDate.of(2025, 1, 1),
      kotikuntaSuomessaAlkaen = None,
      onOikeusValvoaMaksuttomuutta = false,
      onOikeusValvoaKunnalla = false,
    )

  private def mockGetResponseForOids(
    queryOids: Set[String],
    hakutapaKoodiarvo: String = "01",
    responseOppijaOid: Option[String] = None,
  ): Unit = {
    require(queryOids.size == 1, "mockGetResponseForOids supports only a single OID")
    val oid = queryOids.head
    val responseOid = responseOppijaOid.getOrElse(oid)
    val hakukoosteet = HakukoosteExampleData.data
      .filter(_.oppijaOid == oid)
      .map(h => if (responseOppijaOid.isDefined) h.copy(oppijaOid = responseOid) else h)
    val json = OvaraHakukoosteServiceSpec.ovaraJsonFor(hakukoosteet, hakutapaKoodiarvo)
    wireMockServer.stubFor(
      WireMock.get(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
        .withQueryParam("ovara_oppijanumero", WireMock.equalTo(oid))
        .willReturn(WireMock.ok().withBody(json)))
  }

  private def mockPostResponseForOids(queryOids: Set[String]): Unit = {
    val hakukoosteet = HakukoosteExampleData.data.filter(h => queryOids.contains(h.oppijaOid))
    val json = OvaraHakukoosteServiceSpec.ovaraJsonFor(hakukoosteet)
    val expectedBody = queryOids.map(oid => s""""$oid"""").mkString("[", ",", "]")
    wireMockServer.stubFor(
      WireMock.post(WireMock.urlPathEqualTo(ovaraHakukoosteUrl))
        .withRequestBody(WireMock.equalToJson(expectedBody, true, false))
        .willReturn(WireMock.ok().withBody(json)))
  }
}

object OvaraHakukoosteServiceSpec {
  def ovaraJsonFor(hakukoosteet: Seq[Hakukooste], hakutapaKoodiarvo: String = "01"): String =
    hakukoosteet.map(hakukoosteToOvaraJson(_, hakutapaKoodiarvo)).mkString("[", ",", "]")

  private def hakukoosteToOvaraJson(h: Hakukooste, hakutapaKoodiarvo: String): String = {
    val haunAlkamispaivamaara = h.haunAlkamispaivamaara.toLocalDate.toString
    val muokkausaika = h.hakemuksenMuokkauksenAikaleima.map(dt => s""""${dt}+00:00"""").getOrElse("null")
    s"""{
       |  "hakemusOid": "${h.hakemusOid}",
       |  "hakemusUrl": "${h.hakemusUrl}",
       |  "hakemuksenMuokkauksenAikaleima": $muokkausaika,
       |  "email": "${h.email}",
       |  "matkapuhelin": "${h.matkapuhelin}",
       |  "lahiosoite": "${h.lahiosoite}",
       |  "postinumero": "${h.postinumero}",
       |  "postitoimipaikka": ${h.postitoimipaikka.map(s => s""""$s"""").getOrElse("null")},
       |  "maa": ${koodiviiteJson(h.maa)},
       |  "hakuOid": "${h.hakuOid}",
       |  "hakuNimi": ${localizedStringJson(h.hakuNimi)},
       |  "hakutapa": {"koodiarvo": "$hakutapaKoodiarvo", "koodistoUri": "hakutapa"},
       |  "hakutyyppi": ${koodiviiteJson(h.hakutyyppi)},
       |  "aktiivinenHaku": ${h.aktiivinenHaku.getOrElse(false)},
       |  "haunAlkamispaivamaara": "$haunAlkamispaivamaara",
       |  "oppijaOid": "${h.oppijaOid}",
       |  "huoltajanNimi": ${h.huoltajanNimi.map(s => s""""$s"""").getOrElse("null")},
       |  "huoltajanPuhelinnumero": ${h.huoltajanPuhelinnumero.map(s => s""""$s"""").getOrElse("null")},
       |  "huoltajanSahkoposti": ${h.huoltajanSähkoposti.map(s => s""""$s"""").getOrElse("null")},
       |  "hakutoiveet": [${h.hakutoiveet.map(hakutoiveJson).mkString(",")}]
       |}""".stripMargin
  }

  private def hakutoiveJson(t: Hakutoive): String = {
    val koulutuskoodit = t.hakukohdeKoulutuskoodi
      .map(kv => s"""[{"koodiarvo": "${kv.koodiarvo}", "koodistoUri": "${kv.koodistoUri}"}]""")
      .getOrElse("[]")
    val harkinnanvaraisuus = t.harkinnanvaraisuus.getOrElse("EI_HARKINNANVARAINEN_HAKUKOHDE")
    s"""{
       |  "hakukohdeOid": "${t.hakukohdeOid}",
       |  "hakukohdeNimi": ${localizedStringJson(t.hakukohdeNimi)},
       |  "hakutoivenumero": ${t.hakutoivenumero},
       |  "hakukohdeOrganisaatio": "${t.hakukohdeOrganisaatio}",
       |  "organisaatioNimi": ${localizedStringJson(t.organisaatioNimi)},
       |  "koulutusOid": ${t.koulutusOid.map(s => s""""$s"""").getOrElse("null")},
       |  "koulutusNimi": ${localizedStringJson(t.koulutusNimi)},
       |  "hakukohdeKoulutuskoodi": $koulutuskoodit,
       |  "vastaanottotieto": ${t.vastaanottotieto.map(s => s""""$s"""").getOrElse("null")},
       |  "valintatila": ${t.valintatila.map(s => s""""$s"""").getOrElse("null")},
       |  "ilmoittautumistila": ${t.ilmoittautumistila.map(s => s""""$s"""").getOrElse("null")},
       |  "harkinnanvaraisuus": "$harkinnanvaraisuus",
       |  "pisteet": ${t.pisteet.map(_.toString).getOrElse("null")},
       |  "varasijanumero": ${t.varasijanumero.map(_.toString).getOrElse("null")},
       |  "alinHyvaksyttyPistemaara": ${t.alinHyvaksyttyPistemaara.map(_.toString).getOrElse("null")}
       |}""".stripMargin
  }

  private def koodiviiteJson(kv: Option[fi.oph.koski.schema.Koodistokoodiviite]): String =
    kv.map(k => s"""{"koodiarvo": "${k.koodiarvo}", "koodistoUri": "${k.koodistoUri}"}""").getOrElse("null")

  private def localizedStringJson(ls: BlankableLocalizedString): String = {
    val entries = ls.values.map { case (lang, value) => s""""$lang": "${value.replace("\"", "\\\"")}"""" }
    entries.mkString("{", ", ", "}")
  }

  def realResponse(oppijaOid: String): String =
    s"""[
       |  {
       |    "hakemusOid": "1.2.246.562.11.00000000000003301492",
       |    "hakemusUrl": "https://virkailija.testiopintopolku.fi/lomake-editori/applications/search?term=1.2.246.562.11.00000000000003301492",
       |    "hakemuksenMuokkauksenAikaleima": "2026-03-30T21:37:13.544214+03:00",
       |    "email": "testi@testiopintopolku.fi",
       |    "matkapuhelin": "+358481234567",
       |    "lahiosoite": "Testitie 1",
       |    "postinumero": "00100",
       |    "postitoimipaikka": "00100",
       |    "maa": {
       |      "versioituUri": "maatjavaltiot2_246#2",
       |      "koodiarvo": "246",
       |      "koodistoUri": "maatjavaltiot2",
       |      "koodistoVersio": 2,
       |      "nimi": {"fi": "Suomi", "sv": "Finland", "en": "Finland"}
       |    },
       |    "hakuOid": "1.2.246.562.29.00000000000000075761",
       |    "hakuNimi": {
       |      "fi": "Perusopetuksen jälkeisen koulutuksen yhteishaku 2026",
       |      "sv": "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2026",
       |      "en": "Joint application to upper secondary education and preparatory education 2026"
       |    },
       |    "hakutapa": {
       |      "versioituUri": "hakutapa_01#1",
       |      "koodiarvo": "01",
       |      "koodistoUri": "hakutapa",
       |      "koodistoVersio": 1,
       |      "nimi": {"fi": "Yhteishaku", "sv": "Gemensam ansökan", "en": "Joint application"}
       |    },
       |    "hakutyyppi": {
       |      "versioituUri": "hakutyyppi_01#1",
       |      "koodiarvo": "01",
       |      "koodistoUri": "hakutyyppi",
       |      "koodistoVersio": 1,
       |      "nimi": {"fi": "Varsinainen haku", "sv": "Egentlig ansökan"}
       |    },
       |    "aktiivinenHaku": true,
       |    "haunAlkamispaivamaara": "2024-08-31",
       |    "oppijaOid": "$oppijaOid",
       |    "huoltajanNimi": null,
       |    "huoltajanPuhelinnumero": null,
       |    "huoltajanSahkoposti": null,
       |    "hakutoiveet": [
       |      {
       |        "hakukohdeOid": "1.2.246.562.20.00000000000000080037",
       |        "hakukohdeNimi": {"fi": "Musiikkialan perustutkinto (vaativa erityinen tuki)"},
       |        "hakutoivenumero": 2,
       |        "hakukohdeOrganisaatio": "1.2.246.562.10.76662434703",
       |        "organisaatioNimi": {"fi": "Lahti, Villähde"},
       |        "koulutusOid": "1.2.246.562.13.00000000000000010985",
       |        "koulutusNimi": {"fi": "Musiikkialan perustutkinto"},
       |        "hakukohdeKoulutuskoodi": [
       |          {
       |            "versioituUri": "koulutus_321204#12",
       |            "koodiarvo": "321204",
       |            "koodistoUri": "koulutus",
       |            "koodistoVersio": 12,
       |            "nimi": {"fi": "Musiikkialan perustutkinto"}
       |          }
       |        ],
       |        "vastaanottotieto": null,
       |        "valintatila": null,
       |        "ilmoittautumistila": null,
       |        "harkinnanvaraisuus": "EI_HARKINNANVARAINEN_HAKUKOHDE",
       |        "pisteet": 0,
       |        "varasijanumero": 0,
       |        "alinHyvaksyttyPistemaara": null
       |      },
       |      {
       |        "hakukohdeOid": "1.2.246.562.20.00000000000000077691",
       |        "hakukohdeNimi": {"fi": "Ravintola- ja catering-alan perustutkinto (vaativa erityinen tuki)"},
       |        "hakutoivenumero": 1,
       |        "hakukohdeOrganisaatio": "1.2.246.562.10.98880530088",
       |        "organisaatioNimi": {"fi": "Lahti, Ståhlberginkatu"},
       |        "koulutusOid": "1.2.246.562.13.00000000000000011009",
       |        "koulutusNimi": {"fi": "Ravintola- ja catering-alan perustutkinto"},
       |        "hakukohdeKoulutuskoodi": [
       |          {
       |            "versioituUri": "koulutus_381142#7",
       |            "koodiarvo": "381142",
       |            "koodistoUri": "koulutus",
       |            "koodistoVersio": 7,
       |            "nimi": {"fi": "Ravintola- ja catering-alan perustutkinto"}
       |          }
       |        ],
       |        "vastaanottotieto": null,
       |        "valintatila": null,
       |        "ilmoittautumistila": null,
       |        "harkinnanvaraisuus": "EI_HARKINNANVARAINEN_HAKUKOHDE",
       |        "pisteet": 0,
       |        "varasijanumero": 0,
       |        "alinHyvaksyttyPistemaara": null
       |      },
       |      {
       |        "hakukohdeOid": "1.2.246.562.20.00000000000000078067",
       |        "hakukohdeNimi": {"fi": "Kiinteistönhoidon osaamisala"},
       |        "hakutoivenumero": 3,
       |        "hakukohdeOrganisaatio": "1.2.246.562.10.33661993627",
       |        "organisaatioNimi": {"fi": "Lahti, Vipusenkatu"},
       |        "koulutusOid": "1.2.246.562.13.00000000000000010988",
       |        "koulutusNimi": {"fi": "Puhtaus- ja kiinteistöpalvelualan perustutkinto"},
       |        "hakukohdeKoulutuskoodi": [
       |          {
       |            "versioituUri": "koulutus_381141#7",
       |            "koodiarvo": "381141",
       |            "koodistoUri": "koulutus",
       |            "koodistoVersio": 7,
       |            "nimi": {"fi": "Puhtaus- ja kiinteistöpalvelualan perustutkinto"}
       |          }
       |        ],
       |        "vastaanottotieto": null,
       |        "valintatila": null,
       |        "ilmoittautumistila": null,
       |        "harkinnanvaraisuus": "EI_HARKINNANVARAINEN_HAKUKOHDE",
       |        "pisteet": 0,
       |        "varasijanumero": 0,
       |        "alinHyvaksyttyPistemaara": null
       |      }
       |    ]
       |  },
       |  {
       |    "hakemusOid": "1.2.246.562.11.00000000000002632918",
       |    "hakemusUrl": "https://virkailija.testiopintopolku.fi/lomake-editori/applications/search?term=1.2.246.562.11.00000000000002632918",
       |    "hakemuksenMuokkauksenAikaleima": "2026-03-30T21:37:13.544214+03:00",
       |    "email": "testi@testiopintopolku.fi",
       |    "matkapuhelin": "+358481234567",
       |    "lahiosoite": "Testitie 1",
       |    "postinumero": "00100",
       |    "postitoimipaikka": "00100",
       |    "maa": null,
       |    "hakuOid": "1.2.246.562.29.00000000000000056840",
       |    "hakuNimi": {
       |      "fi": "Perusopetuksen jälkeisen koulutuksen yhteishaku 2025",
       |      "sv": "Gemensam ansökan till utbildning efter den grundläggande utbildningen 2025",
       |      "en": "Joint application to upper secondary education and preparatory education 2025"
       |    },
       |    "hakutapa": {
       |      "koodiarvo": "01",
       |      "koodistoUri": "hakutapa"
       |    },
       |    "hakutyyppi": {
       |      "koodiarvo": "01",
       |      "koodistoUri": "hakutyyppi"
       |    },
       |    "aktiivinenHaku": true,
       |    "haunAlkamispaivamaara": "2024-08-31",
       |    "oppijaOid": "$oppijaOid",
       |    "huoltajanNimi": null,
       |    "huoltajanPuhelinnumero": null,
       |    "huoltajanSahkoposti": null,
       |    "hakutoiveet": []
       |  }
       |]""".stripMargin
}

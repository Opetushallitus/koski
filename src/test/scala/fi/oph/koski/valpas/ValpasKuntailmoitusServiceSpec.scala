package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema.{Koodistokoodiviite, OidOrganisaatio, Oppilaitos}
import fi.oph.koski.util.DateOrdering
import fi.oph.koski.valpas.hakukooste.HakukoosteExampleData
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenOppijanYhteystiedot, ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoitusPohjatiedotInput, ValpasPohjatietoYhteystieto}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import fi.oph.koski.valpas.yhteystiedot.{ValpasYhteystietoHakemukselta, ValpasYhteystietoOppijanumerorekisteristä}
import org.scalatest.BeforeAndAfterEach

class ValpasKuntailmoitusServiceSpec extends ValpasTestBase with BeforeAndAfterEach {
  private lazy val kuntailmoitusService = KoskiApplicationForTests.valpasKuntailmoitusService

  override protected def beforeEach() {
    super.beforeEach()
    AuditLogTester.clearMessages
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    super.afterEach()
  }

  private def oppilaitos(oid: String) =
    MockOrganisaatioRepository.getOrganisaatioHierarkia(oid).flatMap(_.toOppilaitos).get

  private def kunta(oid: String) =
    MockOrganisaatioRepository.getOrganisaatioHierarkia(oid).head.toKunta.get

  private val helsinginKaupunki = kunta(MockOrganisaatiot.helsinginKaupunki)

  "Pohjatietojen haku ilman organisaatiota yhdelle oppijalle onnistuu" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(defaultSession)
    result.isRight should equal(true)
  }

  "Pohjatietojen haku yhdellä organisaatiolla yhdelle oppijalle onnistuu" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(OidOrganisaatio(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(defaultSession)
    result.isRight should equal(true)
  }

  "Pohjatietojen haku ilman organisaatiota yhdelle oppijalle palauttaa samat tiedot kuin organisaation kanssa" in {
    val inputOrganisaatiolla = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val resultOrganisaatiolla = kuntailmoitusService.haePohjatiedot(inputOrganisaatiolla)(defaultSession)
    resultOrganisaatiolla.isRight should equal(true)

    val inputIlmanOrganisaatiota = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val resultIlmanOrganisaatiota = kuntailmoitusService.haePohjatiedot(inputIlmanOrganisaatiota)(defaultSession)
    resultIlmanOrganisaatiota.isRight should equal(true)

    resultIlmanOrganisaatiota should equal(resultOrganisaatiolla)
  }

  "Pohjatietojen haku yhdellä organisaatiolla monella oppijalla palauttaa oppijat pyyntöjärjestyksessä" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(OidOrganisaatio(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid, ValpasMockOppijat.kotiopetusMenneisyydessäOppija.oid)
    )

    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    val saadutOppijaOidit = result.map(_.oppijat).map(_.map(_.oppijaOid))

    val expectedOppijaOidit = Seq(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid, ValpasMockOppijat.kotiopetusMenneisyydessäOppija.oid)

    saadutOppijaOidit should equal(Right(expectedOppijaOidit))
  }

  "Pohjatietojen haku ilman organisaatiota aiheuttaa virheen oppijalla, johon ei käyttäjällä ole oikeuksia" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasAapajoenKoulu))
    result should equal(Left(ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")))
  }

  "Pohjatietojen haku ilman organisaatiota aiheuttaa virheen käyttäjälle, jolla on ainoastaan maksuttomuusoikeudet" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä))
    result should equal(Left(ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")))
  }

  "Pohjatietojen haku organisaation kanssa aiheuttaa virheen oppijalla, jonka organisaatioon ei käyttäjällä ole oikeuksia" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(OidOrganisaatio(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasAapajoenKoulu))
    result should equal(Left(ValpasErrorCategory.forbidden.organisaatio("Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin")))
  }

  "Pohjatiedoissa jos inputtina on annettu organisaatio, palautetaan virhe, jos kyseisestä organisaatiosta käsin ei saa tehdä kaikille oppijoille ilmoitusta" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(OidOrganisaatio(oid = MockOrganisaatiot.aapajoenKoulu)),
      oppijaOidit = List(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid, ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasAapajoenKoulu))
    result should equal(Left(ValpasErrorCategory.forbidden.oppijat("Käyttäjällä ei ole oikeuksia kaikkien oppijoiden tietoihin")))
  }

  "Pohjatietojen haku ilman organisaatiota monella oppijalla, joista osalle käytetään slave oidia, epäonnistuu" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)
    )

    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    result should equal(Left(ValpasErrorCategory.forbidden.oppijat("Käyttäjällä ei ole oikeuksia kaikkien oppijoiden tietoihin")))
  }

  "Pohjatietojen haku yhdellä organisaatiolla monella oppijalla, joista osalle käytetään slave oidia, epäonnistuu" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio =  Some(OidOrganisaatio(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)
    )

    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    result should equal(Left(ValpasErrorCategory.forbidden.oppijat("Käyttäjällä ei ole oikeuksia kaikkien oppijoiden tietoihin")))
  }

  "Pohjatiedoissa palautetaan tekijän yhteystiedot, jos ne ovat saatavilla oppijanumerorekisterissä" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result =
      kuntailmoitusService
      .haePohjatiedot(input)(defaultSession)

    val tekijä =
      result.map(_.tekijäHenkilö)

    tekijä should equal(Right(Some(
      ValpasKuntailmoituksenTekijäHenkilö(
        oid = Some(ValpasMockOppijat.käyttäjäValpasJklNormaalikoulu.oid),
        etunimet = Some(ValpasMockOppijat.käyttäjäValpasJklNormaalikoulu.etunimet),
        sukunimi = Some(ValpasMockOppijat.käyttäjäValpasJklNormaalikoulu.sukunimi),
        kutsumanimi = Some(ValpasMockOppijat.käyttäjäValpasJklNormaalikoulu.kutsumanimi),
        email = Some(s"${ValpasMockOppijat.käyttäjäValpasJklNormaalikoulu.kutsumanimi.toLowerCase}@gmail.com"),
        puhelinnumero = Some("0401122334, 09777 888")
      )
    )))
  }

  "Pohjatiedoissa palautetaan yksinkertaiset tekijän yhteystiedot, jos niitä ei ole saatavilla oppijanumerorekisterissä" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.aapajoenKoulu)),
      oppijaOidit = List(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(session(ValpasMockUsers.valpasAapajoenKoulu))

    val tekijä =
      result.map(_.tekijäHenkilö)

    tekijä should equal(Right(Some(
      ValpasKuntailmoituksenTekijäHenkilö(
        oid = Some(ValpasMockUsers.valpasAapajoenKoulu.oid),
        etunimet = Some(ValpasMockUsers.valpasAapajoenKoulu.firstname),
        sukunimi = Some(ValpasMockUsers.valpasAapajoenKoulu.lastname),
        kutsumanimi = None,
        email = None,
        puhelinnumero = None
      )
    )))
  }

  "Pohjatiedoissa palautetaan lista kunnista" in {
    val expectedKunnat = KoskiApplicationForTests.organisaatioService.kunnat

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val kunnat = result.map(_.kunnat).map(_.map(o => OidOrganisaatio(o.oid, o.nimi, o.kotipaikka)))

    kunnat should equal(Right(expectedKunnat))
  }

  "Pohjatiedoissa palautetaan lista maista" in {
    val koodisto = KoskiApplicationForTests.koodistoViitePalvelu.getLatestVersionOptional("maatjavaltiot2").get
    val expectedMaat =
      KoskiApplicationForTests.koodistoViitePalvelu.getKoodistoKoodiViitteet(koodisto)

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val maat = result.map(_.maat)

    maat should equal(Right(expectedMaat))
  }

  "Pohjatiedoissa palautetaan lista yhteydenottokielivalinnoista" in {
    val expectedYhteydenottokielet = Seq(
      Koodistokoodiviite("FI", "kieli"),
      Koodistokoodiviite("SV", "kieli")
    ).map(KoskiApplicationForTests.koodistoViitePalvelu.validate).flatten

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val yhteydenottokielet = result.map(_.yhteydenottokielet)

    yhteydenottokielet should equal(Right(expectedYhteydenottokielet))
  }


  "Pohjatiedoissa hakenut muualle asetetaan trueksi vain, jos kaikki oppijan opiskeluoikeudet on merkitty true:ksi" in {
    // TODO, tämän toteutus puuttuu vielä muualta backendistä
  }

  "Pohjatiedoissa globaalin käyttäjän kyselyssä ilman organisaatiota palautetaan oppijan mahdollisina tekijäorganisaatioina lista kaikista oppijan hakeutumisvelvollisuuden valvonnan piirissä olevista organisaatioista" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    val mahdollisetTekijäOrganisaatiot = result.map(_.oppijat(0).mahdollisetTekijäOrganisaatiot).map(_.toSet)

    val expectedTekijäOrganisaatiot = Set(MockOrganisaatiot.jyväskylänNormaalikoulu, MockOrganisaatiot.aapajoenKoulu)
      .map(oppilaitos)

    mahdollisetTekijäOrganisaatiot should equal(Right(expectedTekijäOrganisaatiot))
  }

  "Pohjatiedoissa yhden organisaation käyttäjän kyselyssä ilman organisaatiota palautetaan oppijan mahdollisina tekijäorganisaatioina lista vain niistä, mihin käyttäjällä on oikeus" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasAapajoenKoulu))

    val mahdollisetTekijäOrganisaatiot = result.map(_.oppijat(0).mahdollisetTekijäOrganisaatiot).map(_.toSet)

    val expectedTekijäOrganisaatiot = Set(
      oppilaitos(MockOrganisaatiot.aapajoenKoulu)
    )

    mahdollisetTekijäOrganisaatiot should equal(Right(expectedTekijäOrganisaatiot))
  }

  "Pohjatiedoissa kuntakäyttäjän kyselyssä ilman organisaatiota palautetaan oppijan mahdollisina tekijäorganisaatioina myös kunnat" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(ValpasMockOppijat.kulosaarenYsiluokkalainen.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasPyhtääJaHelsinki))

    val mahdollisetTekijäOrganisaatiot = result.right.get.oppijat.flatMap(_.mahdollisetTekijäOrganisaatiot).toSet

    val expectedTekijäOrganisaatiot = Set(
      oppilaitos(MockOrganisaatiot.kulosaarenAlaAste),
      helsinginKaupunki,
      kunta(MockOrganisaatiot.pyhtäänKunta)
    )

    mahdollisetTekijäOrganisaatiot should equal(expectedTekijäOrganisaatiot)
  }

  "Pohjatiedoissa annetulla organisaatiolla haettaessa, ei palauteta muita organisaatioita mahdollisina tekijäorganisaatioina" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    val mahdollisetTekijäOrganisaatiot = result.map(_.oppijat(0).mahdollisetTekijäOrganisaatiot).map(_.toSet)

    val expectedTekijäOrganisaatiot = Set(
      oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)
    )

    mahdollisetTekijäOrganisaatiot should equal(Right(expectedTekijäOrganisaatiot))
  }

  "Pohjatiedoissa ylätason mahdollisina tekijäorganisaatioina palautetaan oppijoiden tekijäorganisaatioiden leikkaus" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = None,
      oppijaOidit = List(
        ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid,
        ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid
      )
    )
    val result = kuntailmoitusService.haePohjatiedot(input)(session(ValpasMockUsers.valpasOphPääkäyttäjä))

    val mahdollisetTekijäOrganisaatiot = result.map(_.mahdollisetTekijäOrganisaatiot)

    val expectedTekijäOrganisaatiot = Seq(
      oppilaitos(MockOrganisaatiot.aapajoenKoulu)
    )

    mahdollisetTekijäOrganisaatiot should equal(Right(expectedTekijäOrganisaatiot))
  }

  "Pohjatiedoissa palautetaan oppijan kunta yhteystietojen perusteella" in {
    val oppija = ValpasMockOppijat.luokalleJäänytYsiluokkalainen
    val oppijaOid = oppija.oid

    val expectedKunnat = Seq(Some(helsinginKaupunki), Some(helsinginKaupunki))

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(oppijaOid)
    )

    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val kunnat = result.map(_.oppijat(0).yhteystiedot.map(_.kunta))

    kunnat should equal(Right(expectedKunnat))
  }

  "Pohjatiedoissa ei palauteta kuntaa, jos sitä ei oppijan yhteystietojen perusteella pystytä päättelemään" in {
    val oppija = ValpasMockOppijat.turvakieltoOppija
    val oppijaOid = oppija.oid

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(oppijaOid)
    )

    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val kunnat = result.map(_.oppijat(0).yhteystiedot.map(_.kunta))

    kunnat should equal(Right(Seq(None)))
  }

  "Pohjatiedoissa palautetaan oppijan yhteydenottokielenä suomi, jos oppijan äidinkieli on suomi" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val yhteydenottokieli =
      result.map(_.oppijat(0).yhteydenottokieli)

    yhteydenottokieli should equal(Right(Some(Koodistokoodiviite("FI", "kieli"))))
  }

  "Pohjatiedoissa palautetaan oppijan yhteydenottokielenä ruotsi, jos oppijan äidinkieli on ruotsi" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(Oppilaitos(oid = MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val yhteydenottokieli =
      result.map(_.oppijat(0).yhteydenottokieli)

    yhteydenottokieli should equal(Right(Some(Koodistokoodiviite("SV", "kieli"))))
  }

  "Pohjatiedoissa palautetaan oppijan yhteydenottokielenä suomi, jos oppijan äidinkieli on muu kuin suomi tai ruotsi" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.lukionAloittanut.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val yhteydenottokieli =
      result.map(_.oppijat(0).yhteydenottokieli)

    yhteydenottokieli should equal(Right(Some(Koodistokoodiviite("FI", "kieli"))))
  }

  "Pohjatiedoissa palautetaan turvakielto-tieto oppijalle, jolla sellainen on" in {
    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(ValpasMockOppijat.turvakieltoOppija.oid)
    )
    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val turvakielto =
      result.map(_.oppijat(0).turvakielto)

    turvakielto should equal(Right(true))
  }

  "Pohjatiedoissa palautetaan oppijan uusimman hakemuksen yhteystiedot ja DVV-yhteystiedot" in {
    val oppija = ValpasMockOppijat.luokalleJäänytYsiluokkalainen
    val oppijaOid = oppija.oid
    val oppijanHakukoosteet =
      HakukoosteExampleData.data.filter(hk => hk.oppijaOid == oppijaOid)
    val expectedHakukooste =
      oppijanHakukoosteet.sortBy(h => h.hakemuksenMuokkauksenAikaleima.getOrElse(h.haunAlkamispaivamaara))(DateOrdering.localDateTimeOrdering.reverse).head

    val expectedYhteystiedot = Seq(
      ValpasPohjatietoYhteystieto(
        yhteystietojenAlkuperä = new ValpasYhteystietoHakemukselta(
          hakuNimi = expectedHakukooste.hakuNimi,
          haunAlkamispaivämäärä = expectedHakukooste.haunAlkamispaivamaara,
          hakemuksenMuokkauksenAikaleima = expectedHakukooste.hakemuksenMuokkauksenAikaleima,
          hakuOid = expectedHakukooste.hakuOid,
          hakemusOid = expectedHakukooste.hakemusOid
        ),
        yhteystiedot = ValpasKuntailmoituksenOppijanYhteystiedot(
          puhelinnumero = Some(expectedHakukooste.matkapuhelin),
          email = Some(expectedHakukooste.email),
          lähiosoite = Some(expectedHakukooste.lahiosoite),
          postinumero = Some(expectedHakukooste.postinumero),
          postitoimipaikka = expectedHakukooste.postitoimipaikka,
          maa = Some(Koodistokoodiviite("246", "maatjavaltiot2")) // Suomi
        ),
        kunta = Some(helsinginKaupunki)
      ),
      ValpasPohjatietoYhteystieto(
        yhteystietojenAlkuperä = new ValpasYhteystietoOppijanumerorekisteristä(
          alkuperä = Koodistokoodiviite("alkupera1", "yhteystietojenalkupera"), // VTJ
          tyyppi = Koodistokoodiviite("yhteystietotyyppi1", "yhteystietotyypit"), // Kotiosoite
        ),
        yhteystiedot = ValpasKuntailmoituksenOppijanYhteystiedot(
          puhelinnumero = Some("0401122334"),
          email = Some(s"${oppija.kutsumanimi.toLowerCase}@gmail.com"),
          lähiosoite = Some("Esimerkkitie 10"),
          postinumero = Some("00000"),
          postitoimipaikka = Some("Helsinki"),
          maa = Some(Koodistokoodiviite("188", "maatjavaltiot2")) // Costa Rica
        ),
        kunta = Some(helsinginKaupunki)
      )
    )

    val input = ValpasKuntailmoitusPohjatiedotInput(
      tekijäOrganisaatio = Some(oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)),
      oppijaOidit = List(oppijaOid)
    )

    val result =
      kuntailmoitusService
        .haePohjatiedot(input)(defaultSession)

    val yhteystiedot = result.map(_.oppijat(0).yhteystiedot)

    yhteystiedot should equal(Right(expectedYhteystiedot))
  }
}

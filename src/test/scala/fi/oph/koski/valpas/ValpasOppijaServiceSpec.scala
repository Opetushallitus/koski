package fi.oph.koski.valpas

import java.time.{LocalDate, LocalDateTime}
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService.defaultMockTarkastelupäivä
import fi.oph.koski.valpas.opiskeluoikeusrepository.{MockValpasRajapäivätService, ValpasOpiskeluoikeus, ValpasOppijaLaajatTiedot, ValpasOppijaSuppeatTiedot, ValpasRajapäivätService}
import fi.oph.koski.valpas.valpasrepository.{ValpasExampleData, ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusLaajatTiedotJaOppijaOid}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

class ValpasOppijaServiceSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    super.beforeEach()
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    super.afterEach()
  }

  private val oppijaService = KoskiApplicationForTests.valpasOppijaService
  private val rajapäivätService = KoskiApplicationForTests.valpasRajapäivätService
  private val oppilaitos = MockOrganisaatiot.jyväskylänNormaalikoulu
  private val organisaatioRepository = KoskiApplicationForTests.organisaatioRepository
  private val kuntailmoitusRepository = KoskiApplicationForTests.valpasKuntailmoitusRepository

  // Jyväskylän normaalikoulusta löytyvät näytettävät oppivelvolliset aakkosjärjestyksessä
  private val oppivelvolliset = List(
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true))
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2, "voimassa", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1, "voimassa", false, false, true)
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainen,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true))
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true))
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.kotiopetusMenneisyydessäOpiskeluoikeus, "voimassa", true, true, true))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainen, "voimassa", true, true, true))
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka, "voimassa", true, true, true))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2, "voimassa", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2, "eronnut", false, false, true)
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus, "eronnut", false, false, false)
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllutJollaIlmoitus,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus, "eronnut", false, false, false)
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.lukionAineopinnotAloittanut,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionAineopintojenOpiskeluoikeusAlkaa2021Syksyllä, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.lukionLokakuussaAloittanut,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Lokakuussa, "voimassatulevaisuudessa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppija,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", false, true, false)
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", false, true, false)
      )
    ),
    (
      ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.kesäYsiluokkaKesken, "voimassa", true, true, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen, "voimassa", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true))
    ),
    (
      ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", false, true, true),
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanutJollaVanhaIlmoitus,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanutJaLopettanutJollaIlmoituksia,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true)
      )
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedData) = oppivelvolliset(1)
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(expectedOppija.oid)(defaultSession).toOption.get

    validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
  }

  "getOppijan palauttaman oppijan valintatilat ovat oikein" in {
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)(defaultSession).toOption.get

    val valintatilat = result.hakutilanteet.map(_.hakutoiveet.flatMap(_.valintatila.map(_.koodiarvo)))

    valintatilat shouldBe List(
      List(
        "hylatty",
        "hyvaksytty",
        "peruuntunut",
        "peruuntunut",
        "peruuntunut",
      ),
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)(defaultSession)
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true, true, true)
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka hakukoostekysely epäonnistuisi" in {
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid)(defaultSession).toOption.get
    result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
    validateOppijaLaajatTiedot(
      result.oppija,
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true))
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid)(defaultSession)
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true, true, true)
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      Set(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid),
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeus, "voimassa", false, false, false),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainen, "valmistunut", true, true, true),
        ExpectedData(ValpasOpiskeluoikeusExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true, true, true)
      )
    )
  }

  "getOppijat palauttaa yhden oppilaitoksen oppijat oikein" in {
    val oppijat = oppijaService.getOppijatSuppeatTiedot(oppilaitos)(defaultSession).toOption.get.map(_.oppija)

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedData)) = actualAndExpected
      validateOppijaSuppeatTiedot(
        oppija,
        expectedOppija,
        expectedData)
    }
  }

  "getOppijat palauttaa yhden oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet" in {
    val oppijat = oppijaService.getOppijatSuppeatTiedot(oppilaitos)(session(ValpasMockUsers.valpasOphPääkäyttäjä))
      .toOption.get.map(_.oppija)

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedData)) = actualAndExpected
      validateOppijaSuppeatTiedot(
        oppija,
        expectedOppija,
        expectedData)
    }
  }

  "kuntailmoitukset: getOppija palauttaa kuntailmoituksettoman oppijan ilman kuntailmoituksia" in {
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa.oid)(defaultSession)
      .toOption.get

    oppija.kuntailmoitukset should equal(Seq.empty)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen tekijälle" in {
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.valmistunutYsiluokkalainenJollaIlmoitus.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitukset = Seq(ValpasKuntailmoitusLaajatTiedotLisätiedoilla(expectedIlmoitus, true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdyn kuntailmoituksen kaikki tiedot ilmoituksen kohdekunnalle" in {
    // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
    // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylä normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitukset = Seq(ValpasKuntailmoitusLaajatTiedotLisätiedoilla(expectedIlmoitus, true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: getOppija palauttaa oppijasta tehdystä kuntailmoituksesta vain perustiedot muulle kuin tekijälle tai kunnalle" in {
    // Tässä testissä pitää toistaiseksi temppuilla oppijalla, jolla on monta opiskeluoikeutta, koska pelkällä kuntakäyttäjällä ei vielä ole oikeuksia
    // oppijan tietoihin. Oppijalla on siis ilmoitus Jyväskylän normaalikoulusta Pyhtäälle, ja lisäksi oppija opiskelee Aapajoen peruskoulussa.
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.kahdenKoulunYsiluokkalainenJollaIlmoitus.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitusKaikkiTiedot = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)
    val expectedIlmoitus: ValpasKuntailmoitusLaajatTiedot = karsiPerustietoihin(expectedIlmoitusKaikkiTiedot)

    val expectedIlmoitukset = Seq(ValpasKuntailmoitusLaajatTiedotLisätiedoilla(expectedIlmoitus, true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä master-oidilla" in {
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitukset = Seq(
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta)),
        true
      ),
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla),
        false
      )
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: palauttaa kaikki master- ja slave-oideille tehdyt ilmoitukset pyydettäessä slave-oidilla" in {
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas.oid)(session(ValpasMockUsers.valpasHelsinkiJaAapajoenPeruskoulu))
      .toOption.get

    val expectedIlmoitukset = Seq(
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta),
        true
      ),
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(karsiPerustietoihin(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla)),
        false
      )
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: aktiivinen jos on ilmoituksen tekemisen jälkeen vasta tulevaisuudessa alkava ov-suorittamiseen kelpaava opiskeluoikeus" in {
    val ilmoituksenTekopäivä = date(2021,8,1)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      ValpasMockOppijat.lukionAloittanut.oid,
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
    )
    kuntailmoitusRepository.create(ilmoitus)

    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay),
      true
    )

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: aktiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut 2 kk tai alle" in {
    val ilmoituksenTekopäivä = date(2021,7,15)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      ValpasMockOppijat.lukionAloittanut.oid,
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
    )
    kuntailmoitusRepository.create(ilmoitus)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay),
      true
    )

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: ei-aktiivinen jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaava opiskeluoikeus ja on kulunut yli 2 kk" in {
    val ilmoituksenTekopäivä = date(2021,7,15)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(1)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      ValpasMockOppijat.lukionAloittanut.oid,
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
    )
    kuntailmoitusRepository.create(ilmoitus)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay),
      false
    )

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: aktiivinen, vaikka on yli 2 kk ilmoituksesta, mutta ei ole voimassaolevaa opiskeluoikeutta" in {
    val ilmoituksenTekopäivä = date(2021,6,10)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid,
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta
    )
    kuntailmoitusRepository.create(ilmoitus)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.aapajoenPeruskoulustaValmistunut.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
      .toOption.get

    val expectedIlmoitus = täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoillaAapajoenPeruskoulusta, ilmoituksenTekopäivä.atStartOfDay)
    val expectedIlmoitukset = Seq(ValpasKuntailmoitusLaajatTiedotLisätiedoilla(expectedIlmoitus, true))

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "kuntailmoitukset: aktiivinen, vaikka yli 2 kk ilmoituksesta, jos on ilmoituksen tekemisen jälkeen alkanut ov-suorittamiseen kelpaamaton opiskeluoikeus" in {
    val ilmoituksenTekopäivä = date(2021,6,10)
    val tarkastelupäivä = ilmoituksenTekopäivä.plusMonths(rajapäivätService.kuntailmoitusAktiivisuusKuukausina).plusDays(10)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(ilmoituksenTekopäivä)
    val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      ValpasMockOppijat.lukionAineopinnotAloittanut.oid,
      ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
    )
    kuntailmoitusRepository.create(ilmoitus)

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitus = ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
      täydennäAikaleimallaJaOrganisaatiotiedoilla(ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla, ilmoituksenTekopäivä.atStartOfDay),
      true
    )

    validateKuntailmoitukset(oppija, Seq(expectedIlmoitus))
  }

  "kuntailmoitukset: palautetaan ilmoitukset aikajärjestyksessä ja vain uusin on aktiivinen" in {
    val ilmoituksenTekopäivät = (1 to 3).map(date(2021,8,_))
    val tarkastelupäivä = date(2021,8,30)

    ilmoituksenTekopäivät.map(
      tekopäivä => {
        rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tekopäivä)
        val ilmoitus = ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
          ValpasMockOppijat.lukionAineopinnotAloittanut.oid,
          oppijanPuhelinnumerolla(
            tekopäivä.toString, // Tehdään varmuuden vuoksi ilmoituksista erilaisia myös muuten kuin aikaleiman osalta
            ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla
          )
        )
        kuntailmoitusRepository.create(ilmoitus)
      }
    )

    rajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    val oppija = oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(ValpasMockOppijat.lukionAineopinnotAloittanut.oid)(defaultSession)
      .toOption.get

    val expectedIlmoitukset = Seq(
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-03", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 3).atStartOfDay),
        true
      ),
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-02", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 2).atStartOfDay),
        false
      ),
      ValpasKuntailmoitusLaajatTiedotLisätiedoilla(
        täydennäAikaleimallaJaOrganisaatiotiedoilla(oppijanPuhelinnumerolla("2021-08-01", ValpasExampleData.oppilaitoksenIlmoitusKaikillaTiedoilla), date(2021, 8, 1).atStartOfDay),
        false
      )
    )

    validateKuntailmoitukset(oppija, expectedIlmoitukset)
  }

  "Peruskoulun opo saa haettua oman oppilaitoksen oppijan tiedot" in {
    canAccessOppija(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasJklNormaalikoulu
    ) shouldBe true
  }

  "Peruskoulun opo ei saa haettua toisen oppilaitoksen oppijan tietoja" in {
    canAccessOppija(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasHelsinkiPeruskoulu
    ) shouldBe false
  }

  "Käyttäjä, jolla hakeutumisen tarkastelun oikeudet ja koulutusjärjestäjän organisaatio, näkee oppijan" in {
    canAccessOppija(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasMockUsers.valpasJklYliopisto
    ) shouldBe true
  }

  "Käyttäjä, jolla globaalit oikeudet, näkee oppijan" in {
    canAccessOppija(
      ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe true
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe liian vanhaa oppijaa" in {
    canAccessOppija(
      ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta ennen lain rajapäivää" in {
    canAccessOppija(
      ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe oppijaa, joka on valmistunut peruskoulusta yli 2 kk aiemmin" in {
    canAccessOppija(
      ValpasMockOppijat.yli2kkAiemminPeruskoulustaValmistunut,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla OPPILAITOS_HAKEUTUMINEN globaalit oikeudet, ei näe lukio-oppijaa" in {
    canAccessOppija(
      ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
      ValpasMockUsers.valpasOphHakeutuminenPääkäyttäjä
    ) shouldBe false
  }

  "Käyttäjä, jolla globaalit oikeudet, ei näe lukio-oppijaa" in {
    // TODO: Tämä tulee muuttumaan, kun toteutetaan muut kuin OPPILAITOS_HAKEUTUMINEN oikeudet
    canAccessOppija(
      ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
      ValpasMockUsers.valpasOphPääkäyttäjä
    ) shouldBe false
  }

  def validateOppijaLaajatTiedot(
    oppija: ValpasOppijaLaajatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedData: List[ExpectedData]
  ): Unit = validateOppijaLaajatTiedot(
    oppija,
    expectedOppija,
    Set(expectedOppija.oid),
    expectedData
  )

  def validateOppijaLaajatTiedot(
    oppija: ValpasOppijaLaajatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedOppijaOidit: Set[String],
    expectedData: List[ExpectedData]
  ): Unit = {
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.hetu}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.kaikkiOidit.toSet shouldBe expectedOppijaOidit
      oppija.henkilö.hetu shouldBe expectedOppija.hetu
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi
      oppija.henkilö.turvakielto shouldBe expectedOppija.turvakielto
      oppija.henkilö.äidinkieli shouldBe expectedOppija.äidinkieli

      val expectedOikeutetutOppilaitokset = expectedData.filter(_.onOikeutettuOppilaitos).map(_.opiskeluoikeus.oppilaitos.get.oid).toSet
      oppija.oikeutetutOppilaitokset shouldBe expectedOikeutetutOppilaitokset

      val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
      val maybeExpectedData = expectedData.map(o => Some(o))

      maybeOpiskeluoikeudet.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
        case (element, index) => {
          withClue(s"index ${index}: ") {
            element match {
              case (Some(opiskeluoikeus), Some(expectedData)) =>
                withClue(s"ValpasOpiskeluoikeus(${opiskeluoikeus.oid}/${opiskeluoikeus.oppilaitos.nimi.get("fi")}/${opiskeluoikeus.alkamispäivä}-${opiskeluoikeus.päättymispäivä}): ") {
                  validateOpiskeluoikeus(opiskeluoikeus, expectedData)
                  withClue("alkamispäivä") {
                    Some(opiskeluoikeus.alkamispäivä) shouldBe expectedData.opiskeluoikeus.alkamispäivä.map(_.toString)
                  }
                  withClue("päättymispäivä") {
                    opiskeluoikeus.päättymispäivä shouldBe expectedData.opiskeluoikeus.päättymispäivä.map(_.toString)
                  }
                  withClue("päättymispäiväMerkittyTulevaisuuteen") {
                    opiskeluoikeus.päättymispäiväMerkittyTulevaisuuteen shouldBe expectedData.opiskeluoikeus.päättymispäivä.map(pp => pp.isAfter(defaultMockTarkastelupäivä) )
                  }
                  withClue("näytettäväPerusopetuksenSuoritus") {
                    opiskeluoikeus.näytettäväPerusopetuksenSuoritus shouldBe (
                      expectedData.opiskeluoikeus.tyyppi.koodiarvo == "perusopetus" &&
                        expectedData.tarkastelupäivänTila == "valmistunut" &&
                        expectedData.opiskeluoikeus.päättymispäivä.exists(_.isBefore(defaultMockTarkastelupäivä.plusDays(28)))
                      )
                  }
                }
              case (None, Some(expectedData)) =>
                fail(s"Opiskeluoikeus puuttuu: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${expectedData.opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${expectedData.opiskeluoikeus.tyyppi.koodiarvo}")
              case (Some(opiskeluoikeus), None) =>
                fail(s"Saatiin ylimääräinen opiskeluoikeus: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${opiskeluoikeus.tyyppi.koodiarvo}")
              case _ =>
                fail("Internal error")
            }
          }
        }
      }
    }
  }

  private def validateOppijaSuppeatTiedot(
    oppija: ValpasOppijaSuppeatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedData: List[ExpectedData]
  ): Unit = {
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.sukunimi}/${oppija.henkilö.etunimet}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

      val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
      val maybeExpectedData = expectedData.map(o => Some(o))

      maybeOpiskeluoikeudet.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
        case (element, index) => {
          withClue(s"index ${index}: ") {
            element match {
              case (Some(opiskeluoikeus), Some(expectedData)) =>
                withClue(s"ValpasOpiskeluoikeus(${opiskeluoikeus.oid}/${opiskeluoikeus.oppilaitos.nimi.get("fi")}): ") {
                  validateOpiskeluoikeus(opiskeluoikeus, expectedData)
                }
              case (None, Some(expectedData)) =>
                fail(s"Opiskeluoikeus puuttuu: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${expectedData.opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${expectedData.opiskeluoikeus.tyyppi.koodiarvo}")
              case (Some(opiskeluoikeus), None) =>
                fail(s"Saatiin ylimääräinen opiskeluoikeus: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${opiskeluoikeus.tyyppi.koodiarvo}")
              case _ =>
                fail("Internal error")
            }
          }
        }
      }
    }
  }

  private def validateOpiskeluoikeus(opiskeluoikeus: ValpasOpiskeluoikeus, expectedData: ExpectedData) = {
    withClue("onValvottava") {
      opiskeluoikeus.onValvottava shouldBe expectedData.onValvottavaOpiskeluoikeus
    }
    withClue("oppilaitos.oid") {
      opiskeluoikeus.oppilaitos.oid shouldBe expectedData.opiskeluoikeus.oppilaitos.get.oid
    }
    withClue("tarkastelupäivänTila") {
      opiskeluoikeus.tarkastelupäivänTila.koodiarvo shouldBe expectedData.tarkastelupäivänTila
    }

    val luokkatietoExpectedFromSuoritus = expectedData.opiskeluoikeus match {
      case oo: PerusopetuksenOpiskeluoikeus =>
        oo.suoritukset.flatMap({
          case p: PerusopetuksenVuosiluokanSuoritus => Some(p)
          case _ => None
        }).sortBy(s => s.alkamispäivä)(localDateOptionOrdering).reverse.headOption.map(r => r.luokka)
      // Esim. lukiossa jne. voi olla monta päätason suoritusta, eikä mitään järkevää sorttausparametria päätasolla (paitsi mahdollisesti oleva vahvistus).
      // => oletetaan, että saadaan taulukossa viimeisenä olevan suorituksen ryhmä
      case oo =>
        oo.suoritukset.flatMap({
          case r: Ryhmällinen => Some(r)
          case _ => None
        }).reverse.headOption.flatMap(_.ryhmä)
    }
    withClue("ryhmä") {
      opiskeluoikeus.ryhmä shouldBe luokkatietoExpectedFromSuoritus
    }
  }

  private def täydennäAikaleimallaJaOrganisaatiotiedoilla(
    kuntailmoitus: ValpasKuntailmoitusLaajatTiedot,
    aikaleima: LocalDateTime = rajapäivätService.tarkastelupäivä.atStartOfDay
  ): ValpasKuntailmoitusLaajatTiedot  = {
    // Yksinkertaista vertailukoodia testissä tekemällä samat aikaleiman ja organisaatiodatan täydennykset mitkä tehdään tuotantokoodissa.
    kuntailmoitus.copy(
      aikaleima = Some(aikaleima),
      tekijä = kuntailmoitus.tekijä.copy(
        organisaatio = organisaatioRepository.getOrganisaatio(kuntailmoitus.tekijä.organisaatio.oid).get
      ),
      kunta = organisaatioRepository.getOrganisaatio(kuntailmoitus.kunta.oid).get
    )
  }

  private def karsiPerustietoihin(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot = {
    kuntailmoitus.copy(
      tekijä = kuntailmoitus.tekijä.copy(
        henkilö = None
      ),
      yhteydenottokieli = None,
      oppijanYhteystiedot = None,
      hakenutMuualle = None
    )
  }

  private def oppijanPuhelinnumerolla(puhelinnumero: String, kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot =
    kuntailmoitus.copy(
      oppijanYhteystiedot = Some(kuntailmoitus.oppijanYhteystiedot.get.copy(
        puhelinnumero = Some(puhelinnumero)
      ))
    )

  private def validateKuntailmoitukset(oppija: OppijaHakutilanteillaLaajatTiedot, expectedIlmoitukset: Seq[ValpasKuntailmoitusLaajatTiedotLisätiedoilla]) = {
    def clueMerkkijono(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): String =
      s"${kuntailmoitus.tekijä.organisaatio.nimi.get.get("fi")}=>${kuntailmoitus.kunta.kotipaikka.get.nimi.get.get("fi")}"

    val maybeIlmoitukset = oppija.kuntailmoitukset.map(o => Some(o))
    val maybeExpectedData = expectedIlmoitukset.map(o => Some(o))

    maybeIlmoitukset.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
      case (element, index) => {
        withClue(s"index ${index}: ") {
          element match {
            case (Some(kuntailmoitusLisätiedoilla), Some(expectedData)) =>
              withClue(s"ValpasKuntailmoitusLaajatTiedotLisätiedoilla(${kuntailmoitusLisätiedoilla.kuntailmoitus.id}/${clueMerkkijono(kuntailmoitusLisätiedoilla.kuntailmoitus)}):") {
                withClue("aktiivinen") {
                  kuntailmoitusLisätiedoilla.aktiivinen should equal(expectedData.aktiivinen)
                }
                withClue("kunta") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.kunta should equal(expectedData.kuntailmoitus.kunta)
                }
                withClue("aikaleiman päivämäärä") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.aikaleima.map(_.toLocalDate) should equal(expectedData.kuntailmoitus.aikaleima.map(_.toLocalDate))
                }
                withClue("tekijä") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.tekijä should equal(expectedData.kuntailmoitus.tekijä)
                }
                withClue("yhteydenottokieli") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.yhteydenottokieli should equal(expectedData.kuntailmoitus.yhteydenottokieli)
                }
                withClue("oppijanYhteystiedot") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.oppijanYhteystiedot should equal(expectedData.kuntailmoitus.oppijanYhteystiedot)
                }
                withClue("hakenutMuualle") {
                  kuntailmoitusLisätiedoilla.kuntailmoitus.hakenutMuualle should equal(expectedData.kuntailmoitus.hakenutMuualle)
                }
              }
            case (None, Some(expectedData)) =>
              fail(s"Ilmoitus puuttuu: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(expectedData.kuntailmoitus)}")
            case (Some(kuntailmoitusLisätiedoilla), None) =>
              fail(s"Saatiin ylimääräinen ilmoitus: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(kuntailmoitusLisätiedoilla.kuntailmoitus)}")
            case _ =>
              fail("Internal error")
          }
        }
      }
    }
  }

  private def canAccessOppija(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(session(user)).isRight
}

case class ExpectedData(
  opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
  tarkastelupäivänTila: String,
  onValvottavaOpiskeluoikeus: Boolean,
  onValvottavaOpiskeluoikeusGlobaalilleKäyttäjälle: Boolean,
  onOikeutettuOppilaitos: Boolean
)

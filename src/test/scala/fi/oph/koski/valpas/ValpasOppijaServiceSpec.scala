package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.opiskeluoikeusfixture.{ValpasMockOppijat, ValpasOpiskeluoikeusExampleData}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService.defaultMockTarkastelupäivä
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeus, ValpasOppijaLaajatTiedot, ValpasOppijaSuppeatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}

class ValpasOppijaServiceSpec extends ValpasTestBase {
  private val oppijaService = KoskiApplicationForTests.valpasOppijaService
  private val oppilaitos = MockOrganisaatiot.jyväskylänNormaalikoulu

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
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(ValpasOpiskeluoikeusExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa", false, false, false),
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
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedData) = oppivelvolliset(1)
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(expectedOppija.oid)(defaultSession).toOption.get

    validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
  }

  "getOppijan palauttaman oppijan valintatilat ovat oikein" in {
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)(defaultSession).toOption.get

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
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)(defaultSession)
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
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid)(defaultSession).toOption.get
    result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
    validateOppijaLaajatTiedot(
      result.oppija,
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(ValpasOpiskeluoikeusExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true, true, true))
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid)(defaultSession)
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
    val result = oppijaService.getOppijaHakutilanteillaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
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

  private def canAccessOppija(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaService.getOppijaHakutilanteillaLaajatTiedot(oppija.oid)(session(user)).isRight
}

case class ExpectedData(
  opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
  tarkastelupäivänTila: String,
  onValvottavaOpiskeluoikeus: Boolean,
  onValvottavaOpiskeluoikeusGlobaalilleKäyttäjälle: Boolean,
  onOikeutettuOppilaitos: Boolean
)

package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.{ValpasOpiskeluoikeus, ValpasOpiskeluoikeusSuppeatTiedot, ValpasOppijaLaajatTiedot, ValpasOppijaSuppeatTiedot, ValpasOppilaitos}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.Matchers._

class ValpasOppijaServiceSpec extends ValpasTestBase {
  val hakukoosteService = ValpasHakukoosteService(KoskiApplicationForTests.config)
  val oppijaService = new ValpasOppijaService(KoskiApplicationForTests, hakukoosteService)
  val oppilaitokset = List(MockOrganisaatiot.jyväskylänNormaalikoulu)

  // Jyväskylän normaalikoulusta löytyvät näytettävät oppivelvolliset aakkosjärjestyksessä
  val oppivelvolliset = List(
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true))
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ExpectedData(ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2, "voimassa", true),
        ExpectedData(ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1, "voimassa", true)
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainen,
      List(ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true))
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ExpectedData(ValpasExampleData.kotiopetusMenneisyydessäOpiskeluoikeus, "voimassa", true))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainen, "voimassa", true))
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka, "voimassa", true))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,
      List(
        ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2, "voimassa", true),
        ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2, "eronnut", true)
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ExpectedData(ValpasExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true),
        ExpectedData(ValpasExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus, "eronnut")
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true)
      )
    ),
    (
      ValpasMockOppijat.lukionLokakuussaAloittanut,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Lokakuussa, "voimassatulevaisuudessa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true)
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppija,
      List(
        ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true)
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
      )
    ),
    (
      ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa,
      List(
        ExpectedData(ValpasExampleData.kesäYsiluokkaKesken, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true)
      )
    ),
    (
      ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen,
      List(
        ExpectedData(ValpasExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen, "voimassa", true)
      )
    ),
    (
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true))
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedData) = oppivelvolliset(1)
    val result = oppijaService.getOppijaLaajatTiedot(expectedOppija.oid)(defaultSession()).toOption.get

    validateOppijaLaajatTiedot(result.oppija, expectedOppija, expectedData)
  }

  "getOppija palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
    val result = oppijaService.getOppijaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)(defaultSession())
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true)
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka hakukoostekysely epäonnistuisi" in {
    val result = oppijaService.getOppijaLaajatTiedot(ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid)(defaultSession()).toOption.get
    result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
    validateOppijaLaajatTiedot(
      result.oppija,
      ValpasMockOppijat.hakukohteidenHakuEpäonnistuu,
      List(ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa", true))
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppijaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid)(defaultSession())
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true)
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppijaLaajatTiedot(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
    validateOppijaLaajatTiedot(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut", true),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true)
      )
    )
  }

  "getOppijat palauttaa yhden oppilaitoksen oppijat oikein" in {
    val oppijat = oppijaService.getOppijatSuppeatTiedot(oppilaitokset.toSet)(defaultSession()).toOption.get.map(_.oppija)

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedData)) = actualAndExpected
      validateOppijaSuppeatTiedot(
        oppija,
        expectedOppija,
        expectedData)
    }
  }

  "getOppijat palauttaa useamman oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet" in {
    val queryOids = (oppilaitokset ++ List(MockOrganisaatiot.aapajoenKoulu)).toSet
    val oppijat = oppijaService.getOppijatSuppeatTiedot(queryOids)(session(ValpasMockUsers.valpasOphPääkäyttäjä)).toOption.get.map(_.oppija)

    val expectedOppivelvolliset = (
      oppivelvolliset ++
      List(
        (
          ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
          List(
            ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut", true)
          )
        ),
        (
          ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKoulua,
          List(
            ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen, "voimassa", true),
            ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen, "eronnut", true)
          )
        )
      )
      ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

    oppijat.map(_.henkilö.oid) shouldBe expectedOppivelvolliset.map(_._1.oid)

    (oppijat zip expectedOppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedOppivelvollisuus)) = actualAndExpected
      validateOppijaSuppeatTiedot(
        oppija,
        expectedOppija,
        expectedOppivelvollisuus)
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
  ) = {
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.hetu}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.hetu shouldBe expectedOppija.hetu
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

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
                    opiskeluoikeus.alkamispäivä shouldBe expectedData.opiskeluoikeus.alkamispäivä.map(_.toString)
                  }
                  withClue("päättymispäivä") {
                    opiskeluoikeus.päättymispäivä shouldBe expectedData.opiskeluoikeus.päättymispäivä.map(_.toString)
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

  def validateOppijaSuppeatTiedot(
    oppija: ValpasOppijaSuppeatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedData: List[ExpectedData]
  ) = {
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

  def canAccessOppija(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaService.getOppijaLaajatTiedot(oppija.oid)(session(user)).isRight

  private def session(user: ValpasMockUser)= user.toValpasSession(KoskiApplicationForTests.käyttöoikeusRepository)
  private def defaultSession() = session(ValpasMockUsers.valpasJklNormaalikoulu)
}

case class ExpectedData(
  opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
  tarkastelupäivänTila: String,
  onOikeutettuOppilaitos: Boolean = false
)

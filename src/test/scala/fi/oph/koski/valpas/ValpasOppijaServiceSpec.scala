package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.{MockRajapäivät, ValpasOppija}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.Matchers._

class ValpasOppijaServiceSpec extends ValpasTestBase {
  val oppijaService = new ValpasOppijaService(KoskiApplicationForTests)
  val oppilaitokset = List(MockOrganisaatiot.jyväskylänNormaalikoulu)

  // Jyväskylän normaalikoulusta löytyvät näytettävät oppivelvolliset aakkosjärjestyksessä
  val oppivelvolliset = List(
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus)
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2,
        ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainen,
      List(ValpasExampleData.valmistunutYsiluokkalainen)
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ValpasExampleData.kotiopetusMenneisyydessäOpiskeluoikeus)
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ValpasExampleData.luokallejäänytYsiluokkalainen)
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ValpasExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka)
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,
      List(
        ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2,
        ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ValpasExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
        ValpasExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä,
        ValpasExampleData.valmistunutYsiluokkalainen
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ValpasExampleData.lukionOpiskeluoikeus,
        ValpasExampleData.valmistunutYsiluokkalainen,
        ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu
      )
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedOpiskeluoikeudet) = oppivelvolliset(1)
    val oppija = oppijaService.getOppija(expectedOppija.oid, defaultRajapäivät)(defaultSession())

    validateOppija(
      oppija.get,
      expectedOppija,
      expectedOpiskeluoikeudet)
  }

  "getOppija palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
    val oppija = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid, defaultRajapäivät)(defaultSession())
    validateOppija(
      oppija.get,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(ValpasExampleData.lukionOpiskeluoikeus, ValpasExampleData.valmistunutYsiluokkalainen, ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu)
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val oppija = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid, defaultRajapäivät)(defaultSession())
    validateOppija(
      oppija.get,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(ValpasExampleData.lukionOpiskeluoikeus, ValpasExampleData.valmistunutYsiluokkalainen, ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu)
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val oppija = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid, defaultRajapäivät)(session(ValpasMockUsers.valpasAapajoenKoulu))
    validateOppija(
      oppija.get,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(ValpasExampleData.lukionOpiskeluoikeus, ValpasExampleData.valmistunutYsiluokkalainen, ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu)
    )
  }

  "getOppijat palauttaa yhden oppilaitoksen oppijat oikein" in {
    val oppijat = oppijaService.getOppijat(oppilaitokset.toSet, defaultRajapäivät)(defaultSession()).get.toList

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedOppivelvollisuus)) = actualAndExpected
      validateOppija(
        oppija,
        expectedOppija,
        expectedOppivelvollisuus)
    }
  }

  "getOppijat palauttaa useamman oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet" in {
    val oppijat = oppijaService.getOppijat((oppilaitokset ++ List(MockOrganisaatiot.aapajoenKoulu)).toSet, defaultRajapäivät)(session(ValpasMockUsers.valpasOphPääkäyttäjä)).get.toList

    val expectedOppivelvolliset = (
      oppivelvolliset ++
      List(
        (
          ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
          List(
            ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu
          )
        ),
        (
          ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKoulua,
          List(
            ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen,
            ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen
          )
        )
      )
      ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

    oppijat.map(_.henkilö.oid) shouldBe expectedOppivelvolliset.map(_._1.oid)

    (oppijat zip expectedOppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedOppivelvollisuus)) = actualAndExpected
      validateOppija(
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

  def validateOppija(
    oppija: ValpasOppija,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedOpiskeluoikeudet: List[KoskeenTallennettavaOpiskeluoikeus]
  ) = {
    oppija.henkilö.oid shouldBe expectedOppija.oid
    oppija.henkilö.hetu shouldBe expectedOppija.hetu
    oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
    oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

    val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
    val maybeExpectedOpiskeluoikeudet = expectedOpiskeluoikeudet.map(o => Some(o))

    (maybeOpiskeluoikeudet.zipAll(maybeExpectedOpiskeluoikeudet, None, None)).foreach {
      case (Some(opiskeluoikeus), Some(expectedOpiskeluoikeus)) =>
        opiskeluoikeus.oppilaitos.oid shouldBe expectedOpiskeluoikeus.oppilaitos.get.oid
        opiskeluoikeus.alkamispäivä shouldBe expectedOpiskeluoikeus.alkamispäivä.map(_.toString)
        opiskeluoikeus.päättymispäivä shouldBe expectedOpiskeluoikeus.päättymispäivä.map(_.toString)
        opiskeluoikeus.viimeisinTila.koodiarvo shouldBe expectedOpiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).get

        val luokkatietoExpectedFromSuoritus = expectedOpiskeluoikeus match {
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
        opiskeluoikeus.ryhmä shouldBe luokkatietoExpectedFromSuoritus

      case (None, Some(expectedOpiskeluoikeus)) =>
        fail(s"Opiskeluoikeus puuttuu: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${expectedOpiskeluoikeus.oid} opiskeluoikeus.tyyppi:${expectedOpiskeluoikeus.tyyppi.koodiarvo}")
      case (Some(opiskeluoikeus), None) =>
        fail(s"Saatiin ylimääräinen opiskeluoikeus: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${opiskeluoikeus.tyyppi.koodiarvo}")
      case _ =>
        fail("Internal error")
    }
  }

  def canAccessOppija(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaService
      .getOppija(oppija.oid, defaultRajapäivät)(session(user))
      .isDefined

  private def session(user: ValpasMockUser)= user.toValpasSession(KoskiApplicationForTests.käyttöoikeusRepository)
  private def defaultSession() = session(ValpasMockUsers.valpasJklNormaalikoulu)

  private lazy val defaultRajapäivät = MockRajapäivät()
}

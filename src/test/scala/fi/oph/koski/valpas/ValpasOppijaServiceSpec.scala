package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.ValpasOppija
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
      List(ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa"))
    ),
    (
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      List(
        ExpectedData(ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2, "voimassa"),
        ExpectedData(ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1, "voimassa")
      )
    ),
    (
      ValpasMockOppijat.valmistunutYsiluokkalainen,
      List(ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut"))
    ),
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      List(ExpectedData(ValpasExampleData.kotiopetusMenneisyydessäOpiskeluoikeus, "voimassa"))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainen, "voimassa"))
    ),
    (
      ValpasMockOppijat.luokallejäänytYsiluokkalainenJollaUusiYsiluokka,
      List(ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenJollaUusiYsiluokka, "voimassa"))
    ),
    (
      ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta,
      List(
        ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2, "voimassa"),
        ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2, "eronnut")
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
        ExpectedData(ValpasExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus, "eronnut")
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut")
      )
    ),
    (
      ValpasMockOppijat.lukionLokakuussaAloittanut,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Lokakuussa, "voimassatulevaisuudessa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut")
      )
    ),
    (
      ValpasMockOppijat.turvakieltoOppija,
      List(
        ExpectedData(ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus, "voimassa")
      )
    ),
    (
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
      )
    ),
    (
      ValpasMockOppijat.useampiYsiluokkaSamassaKoulussa,
      List(
        ExpectedData(ValpasExampleData.kesäYsiluokkaKesken, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut")
      )
    ),
    (
      ValpasMockOppijat.eronnutOppijaTarkastelupäivänJälkeen,
      List(
        ExpectedData(ValpasExampleData.eronnutOpiskeluoikeusTarkastelupäivänJälkeen, "voimassa")
      )
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedData) = oppivelvolliset(1)
    val result = oppijaService.getOppija(expectedOppija.oid)(defaultSession()).toOption.get

    validateOppija(result.oppija, expectedOppija, expectedData)
  }

  "getOppija palauttaa oppijan tiedot, vaikka oid ei olisikaan master oid" in {
    val result = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaToinen.oid)(defaultSession())
    validateOppija(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka hakukoostekysely epäonnistuisi" in {
    val result = oppijaService.getOppija(ValpasMockOppijat.lukionAloittanut.oid)(defaultSession()).toOption.get
    result.hakutilanneError.get should equal("Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
    validateOppija(
      result.oppija,
      ValpasMockOppijat.lukionAloittanut,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut")
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaKolmas.oid)(defaultSession())
    validateOppija(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
      )
    )
  }

  "getOppija palauttaa oppijan tiedot, vaikka kysely tehtäisiin master-oidilla, jonka suoriin opiskeluoikeuksiin ei ole pääsyä" in {
    val result = oppijaService.getOppija(ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster.oid)(session(ValpasMockUsers.valpasAapajoenKoulu))
    validateOppija(
      result.toOption.get.oppija,
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      List(
        ExpectedData(ValpasExampleData.lukionOpiskeluoikeus, "voimassa"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainen, "valmistunut"),
        ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
      )
    )
  }

  "getOppijat palauttaa yhden oppilaitoksen oppijat oikein" in {
    val oppijat = oppijaService.getOppijat(oppilaitokset.toSet)(defaultSession()).toOption.get.map(_.oppija)

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedData)) = actualAndExpected
      validateOppija(
        oppija,
        expectedOppija,
        expectedData)
    }
  }

  "getOppijat palauttaa useamman oppilaitoksen oppijat oikein käyttäjälle, jolla globaalit oikeudet" in {
    val queryOids = (oppilaitokset ++ List(MockOrganisaatiot.aapajoenKoulu)).toSet
    val oppijat = oppijaService.getOppijat(queryOids)(session(ValpasMockUsers.valpasOphPääkäyttäjä)).toOption.get.map(_.oppija)

    val expectedOppivelvolliset = (
      oppivelvolliset ++
      List(
        (
          ValpasMockOppijat.aapajoenPeruskoulustaValmistunut,
          List(
            ExpectedData(ValpasExampleData.valmistunutYsiluokkalainenToinenKoulu, "valmistunut")
          )
        ),
        (
          ValpasMockOppijat.luokalleJäänytYsiluokkalainenVaihtanutKoulua,
          List(
            ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen, "voimassa"),
            ExpectedData(ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen, "eronnut")
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
    expectedData: List[ExpectedData]
  ) = {
    // TODO: Tarkista myös valvottavatOpiskeluoikeudet ja oikeutetutOppilaitokset
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.hetu}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.hetu shouldBe expectedOppija.hetu
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

      val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
      val maybeExpectedData = expectedData.map(o => Some(o))

      maybeOpiskeluoikeudet.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
        case (element, index) => {
          withClue(s"index ${index}: ") {
            element match {
              case (Some(opiskeluoikeus), Some(expectedData)) =>
                withClue(s"ValpasOpiskeluoikeus(${opiskeluoikeus.oid}/${opiskeluoikeus.oppilaitos.nimi.get("fi")}/${opiskeluoikeus.alkamispäivä}-${opiskeluoikeus.päättymispäivä}): ") {
                  withClue("oppilaitos.oid") {
                    opiskeluoikeus.oppilaitos.oid shouldBe expectedData.opiskeluoikeus.oppilaitos.get.oid
                  }
                  withClue("alkamispäivä") {
                    opiskeluoikeus.alkamispäivä shouldBe expectedData.opiskeluoikeus.alkamispäivä.map(_.toString)
                  }
                  withClue("päättymispäivä") {
                    opiskeluoikeus.päättymispäivä shouldBe expectedData.opiskeluoikeus.päättymispäivä.map(_.toString)
                  }
                  withClue("viimeisinTila") {
                    opiskeluoikeus.viimeisinTila.koodiarvo shouldBe expectedData.opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).get
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

  def canAccessOppija(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaService.getOppija(oppija.oid)(session(user)).isRight

  private def session(user: ValpasMockUser)= user.toValpasSession(KoskiApplicationForTests.käyttöoikeusRepository)
  private def defaultSession() = session(ValpasMockUsers.valpasJklNormaalikoulu)
}

case class ExpectedData(
  opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
  tarkastelupäivänTila: String
)

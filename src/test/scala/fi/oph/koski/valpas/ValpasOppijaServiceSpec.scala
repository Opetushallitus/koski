package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus}
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.ValpasOppija
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
      List(ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2)
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
//        ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaEdellinen2, // Tämänkin kuuluisi näkyä, mutta ei vielä toteutettu
        ValpasExampleData.luokallejäänytYsiluokkalainenVaihtanutKouluaJälkimmäinen2
      )
    ),
    (
      ValpasMockOppijat.kasiinAstiToisessaKoulussaOllut,
      List(
//        ValpasExampleData.kasiluokkaEronnutKeväällä2020Opiskeluoikeus, // Tämänkin kuuluisi näkyä, mutta ei ole vielä toteutettu
        ValpasExampleData.pelkkäYsiluokkaKeskenKeväällä2021Opiskeluoikeus
      )
    ),
    (
      ValpasMockOppijat.lukionAloittanut,
      List(
        ValpasExampleData.valmistunutYsiluokkalainen,
//        ValpasExampleData.lukionOpiskeluoikeusAlkaa2021Syksyllä // Tämänkin kuuluisi näkyä, mutta ei ole vielä toteutettu
      )
    )
  ).sortBy(item => (item._1.sukunimi, item._1.etunimet))

  "getPeruskoulunValvojalleNäkyväOppija palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedOpiskeluoikeudet) = oppivelvolliset(1)
    val oppija = oppijaService.getOppija(expectedOppija.oid)(session(ValpasMockUsers.valpasJklNormaalikoulu))

    validateOppija(
      oppija.get,
      expectedOppija,
      expectedOpiskeluoikeudet)
  }

  "getPeruskoulunValvojalleNäkyväOppijat palauttaa oikeat tulokset" in {
    val oppijat = oppijaService.getOppijat(oppilaitokset)(session(ValpasMockUsers.valpasJklNormaalikoulu)).toList

    oppijat.map(_.henkilö.oid) shouldBe oppivelvolliset.map(_._1.oid)

    (oppijat zip oppivelvolliset).foreach { actualAndExpected =>
      val (oppija, (expectedOppija, expectedOppivelvollisuus)) = actualAndExpected
      validateOppija(
        oppija,
        expectedOppija,
        expectedOppivelvollisuus)
    }
  }

  def validateOppija(
    oppija: ValpasOppija,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedOpiskeluoikeudet: List[PerusopetuksenOpiskeluoikeus]
  ) = {
    oppija.henkilö.oid shouldBe expectedOppija.oid
    oppija.henkilö.hetu shouldBe expectedOppija.hetu
    oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
    oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

    (oppija.opiskeluoikeudet zip expectedOpiskeluoikeudet).foreach { actualAndExpected => {
      val (opiskeluoikeus, expectedOpiskeluoikeus) = actualAndExpected

      opiskeluoikeus.oppilaitos.oid shouldBe expectedOpiskeluoikeus.oppilaitos.get.oid
      opiskeluoikeus.alkamispäivä shouldBe expectedOpiskeluoikeus.alkamispäivä.map(_.toString)
      opiskeluoikeus.päättymispäivä shouldBe expectedOpiskeluoikeus.päättymispäivä.map(_.toString)
      opiskeluoikeus.viimeisinTila.koodiarvo shouldBe expectedOpiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).get

      val luokkatietoExpectedFromSuoritus = expectedOpiskeluoikeus.suoritukset.flatMap({
        case p: PerusopetuksenVuosiluokanSuoritus if p.koulutusmoduuli.tunniste.koodiarvo == "9" => Some(p)
        case _ => None
      }).sortBy(s => s.alkamispäivä)(localDateOptionOrdering).reverse.head.luokka

      opiskeluoikeus.ryhmä shouldBe Some(luokkatietoExpectedFromSuoritus)
    }}
  }

  private def session(user: ValpasMockUser)= user.toValpasSession(KoskiApplicationForTests.käyttöoikeusRepository)

}

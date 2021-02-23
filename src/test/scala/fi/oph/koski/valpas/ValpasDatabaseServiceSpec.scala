package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus}
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.{ValpasDatabaseService, ValpasOppija}
import org.scalatest.Matchers._

class ValpasDatabaseServiceSpec extends ValpasTestBase {
  val db = new ValpasDatabaseService(KoskiApplicationForTests)
  val oppilaitokset = List(MockOrganisaatiot.jyväskylänNormaalikoulu)

  // Jyväskylän normaalikoulusta löytyvät näytettävät oppivelvolliset aakkosjärjestyksessä
  val oppivelvolliset = List(
    (
      ValpasMockOppijat.kotiopetusMenneisyydessäOppija,
      ValpasExampleData.kotiopetusMenneisyydessäOpiskeluoikeus
    ),
    (
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus
    ),
    (
      ValpasMockOppijat.päällekkäisiäOppivelvollisuuksia,
      ValpasExampleData.oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2
    )
  )

  "getOppivelvollinenHenkilö palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val (expectedOppija, expectedOpiskeluoikeus) = oppivelvolliset(1)
    val oppija = db.getOppivelvollinenHenkilö(expectedOppija.oid, oppilaitokset)

    validateOppija(
      oppija.get,
      expectedOppija,
      expectedOpiskeluoikeus)
  }

  "getOppivelvollinsetHenkilötJaOpiskeluoikeudet palauttaa oikeat tulokset" in {
    val oppijat = db.getOppivelvollinsetHenkilötJaOpiskeluoikeudet(oppilaitokset).toList

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
    expectedOpiskeluoikeus: PerusopetuksenOpiskeluoikeus
  ) = {
    oppija.henkilö.oid shouldBe expectedOppija.oid
    oppija.henkilö.hetu shouldBe expectedOppija.hetu
    oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
    oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

    val opiskeluoikeus = oppija.opiskeluoikeudet.head

    opiskeluoikeus.oppilaitos.oid shouldBe expectedOpiskeluoikeus.oppilaitos.get.oid
    opiskeluoikeus.alkamispäivä shouldBe expectedOpiskeluoikeus.alkamispäivä.map(_.toString)
    opiskeluoikeus.päättymispäivä shouldBe expectedOpiskeluoikeus.päättymispäivä.map(_.toString)
    expectedOpiskeluoikeus.suoritukset.last match {
      case p: PerusopetuksenVuosiluokanSuoritus => opiskeluoikeus.ryhmä shouldBe Some(p.luokka)
    }
  }
}

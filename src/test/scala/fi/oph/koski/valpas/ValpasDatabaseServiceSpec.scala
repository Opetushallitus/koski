package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus
import fi.oph.koski.valpas.fixture.ValpasExampleData
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.{ValpasDatabaseService, ValpasHenkilö, ValpasOpiskeluoikeus, ValpasOppija}
import org.scalatest.FreeSpec
import org.scalatest.Matchers._

class ValpasDatabaseServiceSpec extends FreeSpec {
  val db = new ValpasDatabaseService(KoskiApplicationForTests)
  val oppilaitokset = List(MockOrganisaatiot.jyväskylänNormaalikoulu)

  "getOppivelvollinenHenkilö palauttaa vain annetun oppijanumeron mukaisen oppijan" in {
    val oppija = db.getOppivelvollinenHenkilö(ValpasMockOppijat.päällekkäisiäOppivelvollisuuksia.oid, oppilaitokset)

    validateOppija(
      oppija.get,
      ValpasMockOppijat.päällekkäisiäOppivelvollisuuksia,
      ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
    "9B")
  }

  "getOppivelvollinsetHenkilötJaOpiskeluoikeudet palauttaa oikeat tulokset" in {
    val oppijat = db.getOppivelvollinsetHenkilötJaOpiskeluoikeudet(oppilaitokset)

    oppijat.length shouldBe 2
    validateOppija(
      oppijat.head,
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus,
    "9C")
  }

  def validateOppija(
    oppija: ValpasOppija,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedOpiskeluoikeus: PerusopetuksenOpiskeluoikeus,
    expectedRyhmä: String
  ) = {
    oppija.henkilö.oid shouldBe expectedOppija.oid
    oppija.henkilö.hetu shouldBe expectedOppija.hetu
    oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
    oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

    val opiskeluoikeus = oppija.opiskeluoikeudet.head

    opiskeluoikeus.oppilaitos.oid shouldBe expectedOpiskeluoikeus.oppilaitos.get.oid
    opiskeluoikeus.alkamispäivä shouldBe expectedOpiskeluoikeus.alkamispäivä.map(_.toString)
    opiskeluoikeus.päättymispäivä shouldBe expectedOpiskeluoikeus.päättymispäivä.map(_.toString)
    opiskeluoikeus.ryhmä shouldBe Some(expectedRyhmä)
  }
}

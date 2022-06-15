package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesEsiopetus.ostopalvelu
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{eero, eskari, tero}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec

class VarhaiskasvatusPerustiedotSpec
  extends AnyFreeSpec
    with TestEnvironment
    with DirtiesFixtures
    with SearchTestMethods
    with EsiopetusSpecification {

  override protected def alterFixture(): Unit = tallennaOpiskeluoikeuksiaMajakkaan

  "Varhaiskasvatuksen järjestäjä koulutustoimija" - {
    "Voi hakea omat organisaatiohierarkian ulkopuoliset perustiedot" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiMajakka), MockUsers.helsinkiTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should contain("Päiväkoti Majakka")
    }

    "Voi hakea kaikki omat ostopalvelutiedot" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> KoskiApplicationForTests.organisaatioService.ostopalveluRootOid), MockUsers.helsinkiTallentaja)
      (for {
        perustieto <- perustiedot
        etunimet <- perustieto.henkilö.map(_.etunimet)
        oppilaitos <- perustieto.oppilaitos.nimi.map(_.get("fi"))
      } yield (etunimet, oppilaitos)).sorted should equal(List((eero.etunimet, "Päiväkoti Majakka"), (eskari.etunimet, "Jyväskylän normaalikoulu"), (eskari.etunimet, "Päiväkoti Majakka"), (eskari.etunimet, "Päiväkoti Touhula")))
    }

    "Ei voi hakea muiden luomia organisaatiohierarkian ulkopuolisia perustietoja" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiMajakka), MockUsers.tornioTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should be(empty)
    }
  }

  "Päiväkodin virkailija" - {
    "Voi hakea omaan organisaatioon luodut perustiedot" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiMajakka), MockUsers.majakkaTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should contain("Päiväkoti Majakka")
      val etunimet = perustiedot.flatMap(_.henkilö.map(_.etunimet))
      etunimet should contain(eero.etunimet)
      etunimet should contain(tero.etunimet)
    }
  }

  private def tallennaOpiskeluoikeuksiaMajakkaan = {
    putOpiskeluoikeus(päiväkotiEsiopetus(YleissivistavakoulutusExampleData.päiväkotiMajakka, ostopalvelu), henkilö = asUusiOppija(eero), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    putOpiskeluoikeus(päiväkotiEsiopetus(YleissivistavakoulutusExampleData.päiväkotiMajakka), henkilö = asUusiOppija(tero), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    KoskiApplicationForTests.perustiedotIndexer.sync(refresh = true)
  }
}

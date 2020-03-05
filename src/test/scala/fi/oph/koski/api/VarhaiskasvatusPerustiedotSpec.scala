package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.ExamplesEsiopetus.ostopalvelu
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, eero, tero}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class VarhaiskasvatusPerustiedotSpec extends FreeSpec with BeforeAndAfterAll with SearchTestMethods with EsiopetusSpecification {
  "Varhaiskasvatuksen järjestäjä koulutustoimija" - {
    "Voi hakea omat organisaatiohierarkian ulkopuoliset perustiedot" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiMajakka), MockUsers.helsinkiTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should contain("Päiväkoti Majakka")
    }

    "Voi hakea kaikki ostopalvelutiedot" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> "ostopalvelu/palveluseteli"), MockUsers.helsinkiTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")).sorted should equal(List("Päiväkoti Majakka", "Päiväkoti Touhula"))
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

  override protected def beforeAll(): Unit = tallennaOpiskeluoikeuksiaMajakkaan

  private def tallennaOpiskeluoikeuksiaMajakkaan = {
    putOpiskeluoikeus(päiväkotiEsiopetus(YleissivistavakoulutusExampleData.päiväkotiMajakka, ostopalvelu), henkilö = asUusiOppija(eero), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    putOpiskeluoikeus(päiväkotiEsiopetus(YleissivistavakoulutusExampleData.päiväkotiMajakka), henkilö = asUusiOppija(tero), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    KoskiApplicationForTests.perustiedotSyncScheduler.sync
    KoskiApplicationForTests.perustiedotIndexer.refreshIndex
  }
}

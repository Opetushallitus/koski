package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, tero}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class VarhaiskasvatusPerustiedotSpec extends FreeSpec with BeforeAndAfterAll with SearchTestMethods with EsiopetusSpecification {
  "Varhaiskasvatuksen järjestäjä koulutustoimija" - {
    "Voi hakea omat organisaatiohierarkian ulkopuoliset opiskeluoikeudet" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiTouhula), MockUsers.helsinkiTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should equal(List("Päiväkoti Touhula"))
    }

    "Ei voi hakea muiden luomia organisaatiohierarkian ulkopuoliset opiskeluoikeuksia" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiTouhula), MockUsers.tornioTallentaja)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should be(empty)
    }
  }

  "Päiväkodin virkailija" - {
    "Voi hakea omaan organisaatioon luodut opiskeluoikeudet" in {
      val perustiedot = searchForPerustiedot(Map("opiskeluoikeudenTyyppi" -> "esiopetus", "toimipiste" -> MockOrganisaatiot.päiväkotiTouhula), MockUsers.touholaKatselija)
      perustiedot.flatMap(_.oppilaitos.nimi).map(_.get("fi")) should equal(List("Päiväkoti Touhula", "Päiväkoti Touhula"))
      perustiedot.flatMap(_.henkilö.map(_.etunimet)).toSet should equal(Set(defaultHenkilö.etunimet, tero.etunimet))
    }
  }

  override protected def beforeAll(): Unit = tallennaOpiskeluoikeuksiaTouhulaan

  private def tallennaOpiskeluoikeuksiaTouhulaan = {
    putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula).copy(koulutustoimija = hki), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula), henkilö = asUusiOppija(tero), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
    }
    KoskiApplicationForTests.perustiedotSyncScheduler.sync
    KoskiApplicationForTests.elasticSearch.refreshIndex
  }
}

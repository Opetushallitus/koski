package fi.oph.koski.perftest

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Opiskeluoikeus, Oppilaitos}
import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto
import org.scalatra.test.ClientResponse

import scala.util.Random

object TiedonsiirtoFixtureDataInserter extends FixtureDataInserter {
  lazy val omnia = Oppilaitos(MockOrganisaatiot.omnia)
  lazy val omniaOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus(omnia, AmmatillinenExampleData.autoalanPerustutkinnonSuoritus(omnia)).copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId))
  lazy val opiskeluoikeudet = List.fill(3)(List(omniaOpiskeluoikeus, ExamplesTiedonsiirto.failingOpiskeluoikeus)).flatten
  def opiskeluoikeudet(x: Int) = Random.shuffle(opiskeluoikeudet)
  override def defaultUser = MockUsers.omniaPalvelukäyttäjä
  override def handleResponse(response: ClientResponse, oikeus: Opiskeluoikeus) = {
    if(response.status != 200 && response.status != 403) {
      super.handleResponse(response, oikeus)
    }
  }
}

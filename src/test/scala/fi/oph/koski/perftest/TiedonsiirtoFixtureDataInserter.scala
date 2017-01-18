package fi.oph.koski.perftest

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto

import scala.util.Random

object TiedonsiirtoFixtureDataInserter extends App {
  PerfTestRunner.executeTest(TiedonsiirtoFixtureDataInserterScenario)
}

object TiedonsiirtoFixtureDataInserterScenario extends FixtureDataInserterScenario {
  lazy val omnia = Oppilaitos(MockOrganisaatiot.omnia)
  lazy val omniaOpiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus(omnia, AmmatillinenExampleData.autoalanPerustutkinnonSuoritus(omnia)).copy(lähdejärjestelmänId = Some(AmmatillinenExampleData.winnovaLähdejärjestelmäId))
  lazy val opiskeluoikeudet = List.fill(3)(List(omniaOpiskeluoikeus, ExamplesTiedonsiirto.failingOpiskeluoikeus)).flatten
  def opiskeluoikeudet(x: Int) = Random.shuffle(opiskeluoikeudet)
  override def defaultUser = MockUsers.omniaPalvelukäyttäjä
  override val responseCodes: List[Int] = List(200, 403)
}

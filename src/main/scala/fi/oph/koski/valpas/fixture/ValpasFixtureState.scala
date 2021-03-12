package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.{DatabaseFixtureCreator, DatabaseFixtureState}
import fi.oph.koski.henkilo.OppijaHenkilöWithMasterInfo
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat

class ValpasFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application) {
  val name = "VALPAS"

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = ValpasMockOppijat.defaultOppijat

  protected lazy val databaseFixtureCreator: DatabaseFixtureCreator = new ValpasDatabaseFixtureCreator(application)
}

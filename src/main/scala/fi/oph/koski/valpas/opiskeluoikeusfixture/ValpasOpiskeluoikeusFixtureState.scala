package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.{DatabaseFixtureCreator, DatabaseFixtureState}
import fi.oph.koski.henkilo.OppijaHenkilöWithMasterInfo

class ValpasOpiskeluoikeusFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application) {
  val name = "VALPAS"

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = ValpasMockOppijat.defaultOppijat

  protected lazy val databaseFixtureCreator: DatabaseFixtureCreator = new ValpasOpiskeluoikeusDatabaseFixtureCreator(application)
}

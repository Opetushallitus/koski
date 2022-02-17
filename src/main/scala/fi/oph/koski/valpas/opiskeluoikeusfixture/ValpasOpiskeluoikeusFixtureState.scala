package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.{DatabaseFixtureCreator, DatabaseFixtureState}
import fi.oph.koski.henkilo.OppijaHenkilöWithMasterInfo

object ValpasOpiskeluoikeusFixtureState {
  val name = "VALPAS"
}

class ValpasOpiskeluoikeusFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application) {
  val name = ValpasOpiskeluoikeusFixtureState.name

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = ValpasMockOppijat.defaultOppijat

  lazy val databaseFixtureCreator: DatabaseFixtureCreator = new ValpasOpiskeluoikeusDatabaseFixtureCreator(application)
}

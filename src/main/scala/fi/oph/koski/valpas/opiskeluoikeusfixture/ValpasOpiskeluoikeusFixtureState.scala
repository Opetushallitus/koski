package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.{DatabaseFixtureCreator, DatabaseFixtureState}
import fi.oph.koski.henkilo.{OppijaHenkilöWithMasterInfo, OppijanumerorekisteriKotikuntahistoriaRow}

import scala.collection.mutable

object ValpasOpiskeluoikeusFixtureState {
  val name = "VALPAS"
}

class ValpasOpiskeluoikeusFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application) {
  val name = ValpasOpiskeluoikeusFixtureState.name

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = ValpasMockOppijat.defaultOppijat
  def defaultKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = ValpasMockOppijat.defaultKuntahistoriat
  def defaultTurvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = ValpasMockOppijat.defaultTurvakieltoKuntahistoriat

  lazy val databaseFixtureCreator: DatabaseFixtureCreator = new ValpasOpiskeluoikeusDatabaseFixtureCreator(application)
}

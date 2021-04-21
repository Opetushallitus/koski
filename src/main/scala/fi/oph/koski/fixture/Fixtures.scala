package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{Timing, Wait}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasOpiskeluoikeusFixtureState

object FixtureCreator {
  def generateOppijaOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)
}

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {
  private val raportointikantaService = application.raportointikantaService
  private var currentFixtureState: FixtureState = new NotInitializedFixtureState

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = currentFixtureState.defaultOppijat

  def resetFixtures(fixtureState: FixtureState = koskiSpecificFixtureState): Unit = synchronized {
    if (shouldUseFixtures) {
      application.cacheManager.invalidateAllCaches
      currentFixtureState = fixtureState
      fixtureState.resetFixtures
      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.tiedonsiirtoService.index.deleteAll()

      if (fixtureState == valpasFixtureState) {
        raportointikantaService.loadRaportointikanta(force = true)
        Wait.until { raportointikantaService.isLoadComplete }
      }

      logger.info(s"Reset application fixtures ${fixtureState.name}")
    }
  }

  def shouldUseFixtures = {
    val useFixtures: Boolean = Environment.currentEnvironment(application.config) match {
      case Environment.UnitTest => true
      case Environment.Local => Environment.isUsingLocalDevelopmentServices(application)
      case _ => false
    }

    if (useFixtures && application.masterDatabase.util.databaseIsLarge) {
      throw new RuntimeException(s"Trying to use fixtures against a database with more than ${application.masterDatabase.util.SmallDatabaseMaxRows} rows")
    }
    if (useFixtures && application.perustiedotIndexer.indexIsLarge) {
      throw new RuntimeException(s"Trying to use fixtures against an ElasticSearch index with more than ${application.perustiedotIndexer.SmallIndexMaxRows} rows")
    }
    useFixtures
  }

  def allOppijaOids: List[String] = (koskiSpecificFixtureState.oppijaOids ++ valpasFixtureState.oppijaOids).distinct // oids that should be considered when deleting fixture data

  def getCurrentFixtureStateName() = currentFixtureState.name

  lazy val koskiSpecificFixtureState = new KoskiSpecificFixtureState(application)
  lazy val valpasFixtureState = new ValpasOpiskeluoikeusFixtureState(application)
}

trait FixtureState extends Timing {
  def name: String
  def defaultOppijat: List[OppijaHenkilöWithMasterInfo]
  def resetFixtures: Unit

  def oppijaOids: List[String]
}

class NotInitializedFixtureState extends FixtureState {
  val name = "NOT_INITIALIZED"

  def defaultOppijat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def resetFixtures = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def oppijaOids = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }
}

abstract class DatabaseFixtureState(application: KoskiApplication) extends FixtureState {
  def resetFixtures = {
    timed("Resetting database fixtures") (databaseFixtureCreator.resetFixtures)
    application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetFixtures(defaultOppijat)
  }

  def oppijaOids: List[String] = (
    defaultOppijat.map(_.henkilö.oid) ++ (1 to (defaultOppijat.length) + 100).map(FixtureCreator.generateOppijaOid).toList
    ).distinct

  protected def databaseFixtureCreator: DatabaseFixtureCreator
}

class KoskiSpecificFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application)  {
  val name = "KOSKI_SPECIFIC"

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = KoskiSpecificMockOppijat.defaultOppijat

  protected lazy val databaseFixtureCreator: DatabaseFixtureCreator = new KoskiSpecificDatabaseFixtureCreator(application)
}

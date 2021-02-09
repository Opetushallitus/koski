package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiDatabaseConfig
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.fixture.ValpasFixtureState

object FixtureCreator {
  def generateOppijaOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)
}

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {
  private var currentFixtureState: FixtureState = new NotInitializedFixtureState

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = currentFixtureState.defaultOppijat

  def resetFixtures(fixtureState: FixtureState = koskiSpecificFixtureState) = if(shouldUseFixtures) {
    synchronized {
      application.cacheManager.invalidateAllCaches
      currentFixtureState = fixtureState
      currentFixtureState.resetFixtures
      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.tiedonsiirtoService.index.deleteAll()
      logger.info(s"Reset application fixtures ${currentFixtureState.name}")
    }
  }

  def shouldUseFixtures = {
    val config = application.config
    val useFixtures = if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      KoskiDatabaseConfig(config).isLocal && config.getString("opintopolku.virkailija.url") == "mock"
    }
    if (useFixtures && !Environment.isLocalDevelopmentEnvironment) {
      throw new RuntimeException("Trying to use fixtures when running in a server environment")
    }
    if (useFixtures && application.masterDatabase.databaseIsLarge) {
      throw new RuntimeException("Trying to use fixtures against a database with more than 100 rows")
    }
    if (useFixtures && application.perustiedotIndexer.indexIsLarge) {
      throw new RuntimeException("Trying to use fixtures against an ElasticSearch index with more than 100 rows")
    }
    useFixtures
  }

  def allOppijaOids: List[String] = (koskiSpecificFixtureState.oppijaOids ++ valpasFixtureState.oppijaOids).distinct // oids that should be considered when deleting fixture data

  lazy val koskiSpecificFixtureState = new KoskiSpecificFixtureState(application)
  lazy val valpasFixtureState = new ValpasFixtureState(application)
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
  lazy val name = "KOSKI_SPECIFIC"

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = KoskiSpecificMockOppijat.defaultOppijat

  protected lazy val databaseFixtureCreator: DatabaseFixtureCreator = new KoskiSpecificDatabaseFixtureCreator(application)
}

package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiDatabaseConfig
import fi.oph.koski.fixture.FixtureType.FixtureType
import fi.oph.koski.henkilo.MockOpintopolkuHenkilöFacade
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.fixture.ValpasDatabaseFixtureCreator

object FixtureCreator {
  var currentFixtureType: FixtureType = FixtureType.KOSKI
}

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {

  private val koskiSpecificDatabaseFixtures = new KoskiSpecificDatabaseFixtureCreator(application)
  private val valpasDatabaseFixtures = new ValpasDatabaseFixtureCreator(application)

  def resetFixtures(fixtureType: FixtureType = FixtureType.KOSKI) = if(shouldUseFixtures) {
    synchronized {
      application.cacheManager.invalidateAllCaches
      fixtureType match {
        case FixtureType.KOSKI => {
          FixtureCreator.currentFixtureType = fixtureType
          timed("Resetting database fixtures") (koskiSpecificDatabaseFixtures.resetFixtures)
          application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetKoskiSpecificFixtures
        }
        case FixtureType.VALPAS => {
          FixtureCreator.currentFixtureType = fixtureType
          timed("Resetting database fixtures") (valpasDatabaseFixtures.resetFixtures)
          application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetValpasFixtures
        }
      }
      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.tiedonsiirtoService.index.deleteAll()
      logger.info(s"Reset application fixtures ${FixtureCreator.currentFixtureType}")
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
}

object FixtureType extends Enumeration {
  type FixtureType = Value
  val KOSKI,
    VALPAS = Value
}

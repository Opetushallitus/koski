package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiDatabaseConfig
import fi.oph.koski.henkilo.MockOpintopolkuHenkilöFacade
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {
  private val databaseFixtures = new KoskiDatabaseFixtureCreator(application)

  def resetFixtures = if(shouldUseFixtures) {
    application.cacheManager.invalidateAllCaches
    databaseFixtures.resetFixtures
    application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetFixtures
    application.localizationRepository.asInstanceOf[MockLocalizationRepository].reset
    application.tiedonsiirtoService.deleteAll
    logger.info("Reset application fixtures")
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
    if (useFixtures && application.env.databaseIsLarge) {
      throw new RuntimeException("Trying to use fixtures against a database with more than 100 rows")
    }
    if (useFixtures && application.env.indexIsLarge) {
      throw new RuntimeException("Trying to use fixtures against an ElasticSearch index with more than 100 rows")
    }
    useFixtures
  }
}
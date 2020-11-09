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
    logger.info("Reloaded application fixtures")
  }

  def shouldUseFixtures = {
    val config = application.config
    val useFixtures = if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      KoskiDatabaseConfig(config).isLocal && config.getString("opintopolku.virkailija.url") == "mock"
    }
    if (useFixtures && !Environment.isLocalDevelopmentEnvironment) {
      throw new RuntimeException("Using fixtures is only allowed in local development environment")
    }
    if (useFixtures && application.masterDatabase.databaseIsLarge) {
      throw new RuntimeException("Using fixtures not allowed with a large database (make sure you are using a local database)")
    }
    if (useFixtures && application.perustiedotIndexer.indexIsLarge) {
      throw new RuntimeException("Using fixtures not allowed with a large ElasticSearch index (make sure you are using local Elasticsearch)")
    }
    useFixtures
  }
}

package fi.oph.koski.fixture

import com.typesafe.config.Config
import fi.oph.koski.cache.Cached
import fi.oph.koski.db.{KoskiDatabaseConfig, KoskiDatabase}
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.util.Timing

object Fixtures extends Logging with Timing {
  def resetFixtures(config: Config, database: KoskiDatabase, opiskeluOikeusRepository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository with Cached, validator: KoskiValidator) = if(shouldUseFixtures(config)) {
    timed("resetFixtures") {
      new KoskiDatabaseFixtureCreator(database, opiskeluOikeusRepository, oppijaRepository, validator).resetFixtures
      oppijaRepository.resetFixtures
      logger.info("Reset application fixtures")
    }
  }

  def shouldUseFixtures(config: Config) = {
    if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      KoskiDatabaseConfig(config).isLocal
    }
  }
}

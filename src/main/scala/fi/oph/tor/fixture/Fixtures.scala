package fi.oph.tor.fixture

import com.typesafe.config.Config
import fi.oph.tor.cache.Cached
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.log.Logging
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.tor.TorValidator

object Fixtures extends Logging {
  def resetFixtures(config: Config, database: TorDatabase, opiskeluOikeusRepository: OpiskeluOikeusRepository, oppijaRepository: OppijaRepository with Cached, validator: TorValidator) = if(shouldUseFixtures(config)) {
    new TorDatabaseFixtureCreator(database, opiskeluOikeusRepository, oppijaRepository, validator).resetFixtures
    oppijaRepository.resetFixtures
    logger.info("Reset application fixtures")
  }

  def shouldUseFixtures(config: Config) = {
    if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      config.isLocal
    }
  }
}

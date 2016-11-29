package fi.oph.koski.fixture

import com.typesafe.config.Config
import fi.oph.koski.db.{KoskiDatabase, KoskiDatabaseConfig}
import fi.oph.koski.fixture.Fixtures._
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.KoskiValidator

object Fixtures {
  def shouldUseFixtures(config: Config) = {
    if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      KoskiDatabaseConfig(config).isLocal && !config.hasPath("opintopolku.virkailija.url")
    }
  }
}

class FixtureCreator(config: Config, database: KoskiDatabase, opiskeluOikeusRepository: OpiskeluOikeusRepository, oppijaRepository: HenkilöRepository, validator: KoskiValidator) extends Logging with Timing {
  private val databaseFixtures = new KoskiDatabaseFixtureCreator(database, opiskeluOikeusRepository, oppijaRepository, validator)
  def resetFixtures = if(shouldUseFixtures(config)) {
    timed("resetFixtures") {
      databaseFixtures.resetFixtures
      oppijaRepository.resetFixtures
      logger.info("Reset application fixtures")
    }
  }
}
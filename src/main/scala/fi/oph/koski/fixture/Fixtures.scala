package fi.oph.koski.fixture

import com.typesafe.config.Config
import fi.oph.koski.db.{KoskiDatabase, KoskiDatabaseConfig}
import fi.oph.koski.fixture.Fixtures._
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
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

class FixtureCreator(config: Config, database: KoskiDatabase, OpiskeluoikeusRepository: OpiskeluoikeusRepository, henkilöRepository: HenkilöRepository, validator: KoskiValidator) extends Logging with Timing {
  private val databaseFixtures = new KoskiDatabaseFixtureCreator(database, OpiskeluoikeusRepository, henkilöRepository, validator)
  def resetFixtures = if(shouldUseFixtures(config)) {
    timed("resetFixtures") {
      databaseFixtures.resetFixtures
      logger.info("Reset application fixtures")
    }
  }
}
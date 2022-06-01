package fi.oph.koski.db

import fi.oph.koski.log.Logging
import org.flywaydb.core.Flyway

object Migration extends Logging {
  def migrateSchema(config: DatabaseConfig): Unit = {
    config.migrationLocations match {
      case Some(locations) => doMigrate(config, locations)
      case None => logger.info(s"No migrations defined for ${config.getClass}")
    }
  }

  private def doMigrate(config: DatabaseConfig, locations: String): Unit = {
    logger.info(s"Running migrations for ${config.dbname} from $locations")
    createFlyway(config, locations).migrate()
  }

  private def createFlyway(config: DatabaseConfig, locations: String) = {
    val flyway = new Flyway
    flyway.setLocations(locations)
    flyway.setDataSource(config.url(useSecretsManagerProtocol = false), config.user, config.password)
    flyway.setSchemas(config.schemaName)
    flyway.setValidateOnMigrate(false)
    flyway
  }
}

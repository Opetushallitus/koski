package fi.oph.koski.db

import fi.oph.koski.log.Logging
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.jdbc.DriverDataSource

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
    // Flyway's 3-arg setDataSource autodetects the driver from the URL prefix
    // using a hardcoded list, which doesn't include the AWS Wrapper's
    // jdbc:aws-wrapper:postgresql:// scheme. Provide the driver class explicitly.
    flyway.setDataSource(new DriverDataSource(
      Thread.currentThread.getContextClassLoader,
      driverClassFor(config),
      config.url(useIamProtocol = config.useIamAuth),
      config.user,
      config.password
    ))
    flyway.setSchemas(config.schemaName)
    flyway.setValidateOnMigrate(false)
    flyway
  }

  private def driverClassFor(config: DatabaseConfig): String =
    if (config.useIamAuth) "software.amazon.jdbc.Driver" else "org.postgresql.Driver"
}

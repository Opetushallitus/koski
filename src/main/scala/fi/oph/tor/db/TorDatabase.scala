package fi.oph.tor.db

import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.PortChecker
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import sys.process._

object TorDatabase extends Logging {
  type DB = PostgresDriver.backend.DatabaseDef


  def forConfig(config: DatabaseConfig)(implicit executor: AsyncExecutor): DB = {
    if (!isDbRunning(config)) {
      startEmbedded(config)
    }
    createDatabase(config)
    createUser(config)
    migrateSchema(config)
    Database.forURL(config.url, config.user, config.password, executor = executor)
  }

  private def isDbRunning(config: DatabaseConfig) = {
    if (config.isRemote) {
      logger.info("Using remote PostgreSql database at " + config.host + ":" + config.port)
      true
    } else if (!PortChecker.isFreeLocalPort(config.port)) {
      logger.info("PostgreSql already running on port " + config.port)
      true
    } else {
      false
    }
  }

  private def startEmbedded(config: DatabaseConfig) = {
     new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start
  }

  private def createDatabase(config: DatabaseConfig) = {
    val dbName = config.databaseName
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName" !;
  }
  /*
    createdb -T template0 -E UTF-8 tor
    createdb -T template0 -E UTF-8 tortest
    createuser -s tor -P  (salasanaksi tor)
   */

  private def createUser(config: DatabaseConfig) = {
    val userName = config.user
    s"createuser -s $userName -w"!
  }

  private def migrateSchema(config: DatabaseConfig) = {
    try {
      val flyway = new Flyway
      flyway.setDataSource(config.url, config.user, config.password)
      flyway.setSchemas("tor")
      flyway.setValidateOnMigrate(false)
      flyway.migrate
    } catch {
      case e: Exception => logger.warn("Migration failure", e)
    }
  }
}






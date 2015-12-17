package fi.oph.tor.db

import com.typesafe.config.Config
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.PortChecker
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.sys.process._

object TorDatabase {
  type DB = PostgresDriver.backend.DatabaseDef

  implicit class TorDatabaseConfig(c: Config) {
    val config = c.getConfig("db")
    val host: String = config.getString("host")
    val port: Int = config.getInt("port")
    val password: String = config.getString("password")
    val dbName: String = config.getString("name")
    val user: String = config.getString("user")
    val url: String = config.getString("url")
    def isLocal = host == "localhost"
    def isRemote = !isLocal
    def toSlickDatabase = Database.forConfig("", config)
  }
}

class TorDatabase(val config: Config) extends Logging {
  import TorDatabase._

  val serverProcess = startLocalDatabaseServerIfNotRunning

  if (!config.isRemote) {
    createDatabase
    createUser
  }

  val db: DB = config.toSlickDatabase

  migrateSchema

  private def startLocalDatabaseServerIfNotRunning: Option[PostgresRunner] = {
    if (!isDbRunning) {
      Some(startEmbedded)
    } else {
      None
    }
  }

  private def isDbRunning = {
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

  private def startEmbedded: PostgresRunner = {
    new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start
  }

  private def createDatabase = {
    val dbName = config.dbName
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName" !;
  }

  private def createUser = {
    val user = config.user
    s"createuser -s $user -w"!
  }

  private def migrateSchema = {
    try {
      val flyway = new Flyway
      flyway.setDataSource(config.url, config.user, config.password)
      flyway.setSchemas(config.user)
      flyway.setValidateOnMigrate(false)
      if (System.getProperty("tor.db.clean", "false").equals("true")) {
        flyway.clean
      }
      flyway.migrate
    } catch {
      case e: Exception => logger.warn("Migration failure", e)
    }
  }
}





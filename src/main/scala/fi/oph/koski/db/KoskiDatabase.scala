package fi.oph.koski.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.Environment
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Futures
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.sys.process._

object KoskiDatabase {
  type DB = PostgresDriver.backend.DatabaseDef

  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config))

  def replica(config: Config, master: KoskiDatabase): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config, readOnly = true))
}

case class KoskiDatabaseConfig(c: Config, readOnly: Boolean = false, raportointi: Boolean = false) {
  private val masterHost: String = c.getString("db.host")
  private val replicaHost: String = if (c.hasPath("db.replica.host")) c.getString("db.replica.host") else masterHost
  private val raportointiHost: String = if (c.hasPath("db.raportointi.host")) c.getString("db.raportointi.host") else masterHost
  private val masterPort: Int = c.getInt("db.port")
  private val replicaPort: Int = if (c.hasPath("db.replica.port")) c.getInt("db.replica.port") else masterPort
  private val raportointiPort: Int = if (c.hasPath("db.raportointi.port")) c.getInt("db.raportointi.port") else masterPort
  private val masterDbName = c.getString("db.name")
  private val raportointiDbName = if (c.hasPath("db.raportointi.name")) c.getString("db.raportointi.name") else masterDbName

  val (host, port, dbName, poolName) = (raportointi, readOnly) match {
    case (true, _) => (raportointiHost, raportointiPort, raportointiDbName, "koskiRaportointiPool")
    case (false, false) => (masterHost, masterPort, masterDbName, "koskiMasterPool")
    case (false, true) => (replicaHost, replicaPort, masterDbName, "koskiReplicaPool")
  }
  val jdbcDriverClassName = "org.postgresql.Driver"
  val password: String = c.getString("db.password")
  val user: String = c.getString("db.user")
  val jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName

  var config = c.getConfig("db")
    .withValue("poolName", fromAnyRef(poolName))
    .withValue("readOnly", fromAnyRef(readOnly))
    .withValue("url", fromAnyRef(jdbcUrl))
    .withValue("numThreads", fromAnyRef(Pools.dbThreads))
  if (raportointi && c.hasPath("db.raportointi.properties"))
    config = config.withValue("properties", c.getObject("db.raportointi.properties"))

  val url: String = config.getString("url")
  def isLocal = host == "localhost"
  def isRemote = !isLocal
  def toSlickDatabase = Database.forConfig("", config)
}


class KoskiDatabase(val config: KoskiDatabaseConfig) extends Logging {
  val serverProcess = startLocalDatabaseServerIfNotRunning

  if (!config.isRemote && !config.readOnly) {
    createDatabase
    createUser
  }

  val db: DB = config.toSlickDatabase

  if (!config.readOnly) {
    migrateSchema
  }

  private def startLocalDatabaseServerIfNotRunning: Option[PostgresRunner] = {
    if (config.isLocal) {
      Some(new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start)
    } else {
      None
    }
  }

  private def createDatabase = {
    val dbName = config.dbName
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName" !;
  }

  private def createUser = {
    val user = config.user
    val port = config.port
    s"createuser -p $port -s $user -w"!
  }

  private def migrateSchema = {
    try {
      val flyway = new Flyway
      flyway.setDataSource(config.url, config.user, config.password)
      flyway.setSchemas(config.user)
      flyway.setValidateOnMigrate(false)
      if (System.getProperty("koski.db.clean", "false").equals("true")) {
        flyway.clean
      }
      if (Environment.databaseIsLarge(db) && Environment.isLocalDevelopmentEnvironment) {
        logger.warn("Skipping database migration for database larger than 100 rows, when running in local development environment")
      } else {
        flyway.migrate
      }
    } catch {
      case e: Exception => logger.warn(e)("Migration failure")
    }
  }
}





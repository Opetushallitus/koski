package fi.oph.koski.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Pools
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.sys.process._

object KoskiDatabase {
  type DB = PostgresDriver.backend.DatabaseDef

  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config))

  def replica(config: Config, master: KoskiDatabase): KoskiDatabase = {
    val readonlyConfig = KoskiDatabaseConfig(config, readOnly = true)
    if (readonlyConfig.hasReplicaHost) {
      new KoskiDatabase(readonlyConfig)
    } else {
      master
    }
  }
}

case class KoskiDatabaseConfig(c: Config, readOnly: Boolean = false) {
  private val masterHost: String = c.getString("db.host")
  private lazy val replicaHost: String = c.getString("db.replica.host")
  private val masterPort: Int = c.getInt("db.port")
  private val replicaPort: Int = if (c.hasPath("db.replica.port")) c.getInt("db.replica.port") else masterPort

  val host: String = if (readOnly) replicaHost else masterHost
  val port: Int =  if (readOnly) replicaPort else masterPort
  val dbName: String = c.getString("db.name")
  val jdbcDriverClassName = "org.postgresql.Driver"
  val password: String = c.getString("db.password")
  val user: String = c.getString("db.user")
  val jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName  + "?user=" + user + "&password=" + password

  val config = c.getConfig("db")
    .withValue("url", fromAnyRef(jdbcUrl))
    .withValue("numThreads", fromAnyRef(Pools.dbThreads))

  val url: String = config.getString("url")
  def isLocal = host == "localhost"
  def isRemote = !isLocal
  def toSlickDatabase = Database.forConfig("", config)
  def hasReplicaHost = c.hasPath("db.replica.host")
}


class KoskiDatabase(val config: KoskiDatabaseConfig) extends Logging {
  val serverProcess = startLocalDatabaseServerIfNotRunning

  if (!config.isRemote) {
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
      flyway.migrate
    } catch {
      case e: Exception => logger.warn(e)("Migration failure")
    }
  }
}





package fi.oph.koski.db

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigValueFactory._
import fi.oph.common.log.{Logging, NotLoggable}
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.executors.Pools
import fi.oph.koski.raportointikanta.Schema
import fi.oph.koski.util.Futures
import org.flywaydb.core.Flyway
import org.postgresql.util.PSQLException
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.sys.process._

case class DatabaseConfig(password: String, dbname: String, port: Int, host: String, username: String) extends NotLoggable

object KoskiDatabase {
  type DB = PostgresProfile.backend.DatabaseDef

  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config))

  def replica(config: Config, master: KoskiDatabase): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config, readOnly = true))
}

case class KoskiDatabaseConfig(c: Config, readOnly: Boolean = false, raportointiSchema: Option[Schema] = None) {
  private val useSecretsManager = Environment.usesAwsSecretsManager
  private val baseConfig = c.getConfig("db").withoutPath("replica").withoutPath("raportointi")
  private val configFromFile = (raportointiSchema, readOnly) match {
    case (Some(schema), _) =>
      c.getConfig("db.raportointi")
        .withValue("poolName", fromAnyRef(s"koskiRaportointiPool-${schema.name}"))
        .withFallback(baseConfig)
    case (_, false) => baseConfig
    case (_, true) => c.getConfig("db.replica").withFallback(baseConfig)
  }

  private lazy val envVarForSecretId = raportointiSchema match {
    case Some(_) => "DB_RAPORTOINTI_SECRET_ID"
    case _ => "DB_KOSKI_SECRET_ID"
  }
  private lazy val cachedSecretsClient = new SecretsManager
  private lazy val secretId = cachedSecretsClient.getSecretId("Koski DB secrets", envVarForSecretId)

  val DatabaseConfig(password, dbname, port, host: String, username) = {
    if (useSecretsManager) {
      val dbSecretFromSecretsManager = cachedSecretsClient.getStructuredSecret[DatabaseConfig](secretId)
      val replicaHost = sys.env.get("DB_KOSKI_REPLICA_HOST").getOrElse(throw new RuntimeException("Secrets manager enabled for DB secrets but environment variable DB_KOSKI_REPLICA_HOST not set"))

      if (readOnly) {
        dbSecretFromSecretsManager.copy(host = replicaHost)
      } else {
        dbSecretFromSecretsManager
      }
    } else {
      DatabaseConfig(
        configFromFile.getString("password"),
        configFromFile.getString("name"),
        configFromFile.getInt("port"),
        configFromFile.getString("host"),
        configFromFile.getString("user")
      )
    }
  }

  private val otherConfig = ConfigFactory.empty
    .withValue("url", fromAnyRef(s"jdbc:postgresql://$host:$port/$dbname"))
    .withValue("readOnly", fromAnyRef(readOnly))
    .withValue("numThreads", fromAnyRef(Pools.dbThreads))

  private val config = {
    if (useSecretsManager) {
      configFromFile
        .withValue("driverClassName", fromAnyRef("com.amazonaws.secretsmanager.sql.AWSSecretsManagerPostgreSQLDriver"))
        .withValue("user", fromAnyRef(secretId))
        .withValue("url", fromAnyRef(s"jdbc-secretsmanager:postgresql://$host:$port/$dbname"))
        .withFallback(otherConfig)
    } else {
      configFromFile.withFallback(otherConfig)
    }
  }

  def isLocal: Boolean = config.getString("host") == "localhost" && !useSecretsManager
  def isRemote: Boolean = !isLocal
  def toSlickDatabase = Database.forConfig("", config)
}


class KoskiDatabase(val config: KoskiDatabaseConfig) extends Logging {
  val serverProcess: Option[PostgresRunner] = startLocalDatabaseServerIfNotRunning

  if (!config.isRemote && !config.readOnly) {
    createDatabase
    createUser
  }

  val db: DB = config.toSlickDatabase

  if (!config.readOnly) {
    migrateSchema
  }

  private def startLocalDatabaseServerIfNotRunning: Option[PostgresRunner] = {
    if (config.isLocal && !config.readOnly) {
      Some(new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start)
    } else {
      None
    }
  }

  private def createDatabase = {
    val dbName = config.dbname
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName".!
  }

  private def createUser = {
    val user = config.username
    val port = config.port
    s"createuser -p $port -s $user -w".!
  }

  private def migrateSchema = {
    try {
      val flyway = new Flyway
      flyway.setDataSource(s"jdbc:postgresql://${config.host}:${config.port}/${config.dbname}", config.username, config.password)
      flyway.setSchemas(config.username)
      flyway.setValidateOnMigrate(false)
      if (System.getProperty("koski.db.clean", "false").equals("true")) {
        flyway.clean()
      }
      if (Environment.isLocalDevelopmentEnvironment && databaseIsLarge) {
        logger.warn("Skipping database migration for database larger than 100 rows, when running in local development environment")
      } else {
        flyway.migrate
      }
    } catch {
      case e: Exception => logger.warn(e)("Migration failure")
    }
  }

  def databaseIsLarge: Boolean = {
    try {
      val count = Futures.await(db.run(sql"select count(id) from opiskeluoikeus".as[Int]))(0)
      count > 500
    } catch {
      case e: PSQLException =>
        if (e.getMessage.contains("""relation "opiskeluoikeus" does not exist""")) {
          false // Allow for an uninitialized db
        } else {
          throw e
        }
    }
  }
}





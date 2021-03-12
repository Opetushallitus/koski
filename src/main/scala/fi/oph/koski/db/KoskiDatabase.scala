package fi.oph.koski.db

import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import org.flywaydb.core.Flyway
import org.postgresql.util.PSQLException
import slick.jdbc.PostgresProfile.api._

import scala.sys.process._


object KoskiDatabase {
  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(new KoskiDatabaseConfig(config), isReplica = false)

  def replica(config: Config): KoskiDatabase =
    new KoskiDatabase(new KoskiReplicaConfig(config), isReplica = true)
}

case class LocalDatabaseRunner(config: DatabaseConfig) {
  def initLocalPostgres(): Unit = {
    new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start
    createDatabase()
    createUser()
  }

  private def createDatabase(): Unit = {
    val dbName = config.dbname
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName".!
  }

  private def createUser(): Unit = {
    val user = config.user
    val port = config.port
    s"createuser -p $port -s $user -w".!
  }
}

class KoskiDatabase(config: DatabaseConfig, isReplica: Boolean) extends Logging {
  val isLocal: Boolean = config.isLocal

  if (config.isLocal && !isReplica) {
    LocalDatabaseRunner(config).initLocalPostgres()
  }

  val db: DB = config.toSlickDatabase

  val util: DatabaseUtilQueries = DatabaseUtilQueries(db)

  if (!isReplica) {
    migrateSchema
  }

  private def migrateSchema: Unit = {
    if (config.isLocal && util.databaseIsLarge) {
      // Prevent running migrations against a (large) remote database when running locally
      logger.error("Migration not allowed with a large database in local development environment")
    } else {
      val flyway = new Flyway
      flyway.setDataSource(config.url, config.user, config.password)
      flyway.setSchemas(config.user)
      flyway.setValidateOnMigrate(false)
      try {
        flyway.migrate
      } catch {
        case e: Exception => logger.error(e)("Migration failure")
      }
    }
  }
}

case class DatabaseUtilQueries(db: DB) extends KoskiDatabaseMethods {
  val SmallDatabaseMaxRows: Int = 500

  def databaseIsLarge: Boolean = {
    try {
      val count = runDbSync(sql"select count(id) from opiskeluoikeus".as[Int])(0)
      count > SmallDatabaseMaxRows
    } catch {
      case e: PSQLException if e.getMessage.contains("""relation "opiskeluoikeus" does not exist""") =>
        false // Allow for an uninitialized db
    }
  }
}


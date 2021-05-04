package fi.oph.koski.db

import com.typesafe.config.Config
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

class KoskiDatabase(protected val config: DatabaseConfig, isReplica: Boolean) extends Database {
  override final val smallDatabaseMaxRows = 500

  override protected lazy val dbSizeQuery = KoskiTables.OpiskeluOikeudet.length.result

  val isLocal: Boolean = config.isLocal

  if (config.isLocal && !isReplica) {
    LocalDatabaseRunner(config).initLocalPostgres()
  }
}

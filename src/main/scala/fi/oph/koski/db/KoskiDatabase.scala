package fi.oph.koski.db

import com.typesafe.config.Config
import slick.jdbc.PostgresProfile.api._


object KoskiDatabase {
  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(new KoskiDatabaseConfig(config), isReplica = false)

  def replica(config: Config): KoskiDatabase =
    new KoskiDatabase(new KoskiReplicaConfig(config), isReplica = true)
}

class KoskiDatabase(protected val config: DatabaseConfig, isReplica: Boolean) extends Database {
  override final val smallDatabaseMaxRows = 500

  override protected lazy val dbSizeQuery = KoskiTables.KoskiOpiskeluOikeudet.length.result

  val isLocal: Boolean = config.isLocal
}

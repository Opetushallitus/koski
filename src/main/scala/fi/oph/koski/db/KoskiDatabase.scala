package fi.oph.koski.db

import com.typesafe.config.Config
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.{Duration, DurationInt}


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

  def replayLag: Duration =
    QueryMethods.runDbSync(
        db,
        sql"select extract(epoch from replay_lag) as replay_lag from pg_stat_replication".as[Double](GetResult(_.nextDouble))
      ).headOption
      .map(_.toInt)
      .getOrElse(0)
      .seconds
}

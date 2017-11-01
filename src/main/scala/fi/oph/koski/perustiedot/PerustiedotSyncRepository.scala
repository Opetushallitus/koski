package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}
import slick.dbio.Effect
import slick.sql.FixedSqlAction

import scala.concurrent.Future

class PerustiedotSyncRepository(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  def syncLater(opiskeluoikeudet: Seq[Int]) = runDbSync(PerustiedotSync ++= opiskeluoikeudet.map(PerustiedotSyncRow(_)))

  def syncAction(opiskeluoikeusId: Int): FixedSqlAction[Int, NoStream, Effect.Write] = PerustiedotSync += PerustiedotSyncRow(opiskeluoikeusId)

  def needSyncing: Seq[PerustiedotSyncRow] = runDbSync(PerustiedotSync.result)

  def delete(ids: Seq[Int]): Int =
    runDbSync(PerustiedotSync.filter(_.id inSetBind ids).delete)
}


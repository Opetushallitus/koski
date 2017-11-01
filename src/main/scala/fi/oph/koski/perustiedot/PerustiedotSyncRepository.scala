package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}
import rx.lang.scala.Observable
import slick.dbio.Effect
import slick.sql.FixedSqlAction

class PerustiedotSyncRepository(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  def syncLater(opiskeluoikeudet: Seq[Int]) = runDbSync(PerustiedotSync ++= opiskeluoikeudet.map(PerustiedotSyncRow(_)))

  def syncAction(opiskeluoikeusId: Int): FixedSqlAction[Int, NoStream, Effect.Write] = PerustiedotSync += PerustiedotSyncRow(opiskeluoikeusId)

  def needSyncing: Observable[PerustiedotSyncRow] = {
    streamingQuery(PerustiedotSync)
  }

  def delete(maxId: Int): Int =
    runDbSync(PerustiedotSync.filter(_.id <= maxId).delete)
}


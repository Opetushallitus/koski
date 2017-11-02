package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{BackgroundExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}
import org.json4s.JValue
import slick.dbio.Effect
import slick.sql.FixedSqlAction

class PerustiedotSyncRepository(val db: DB) extends BackgroundExecutionContext with KoskiDatabaseMethods {
  def syncAgain(opiskeluoikeudet: Seq[JValue], upsert: Boolean) = runDbSync(PerustiedotSync ++= opiskeluoikeudet.map { oo => PerustiedotSyncRow(opiskeluoikeusId = OpiskeluoikeudenPerustiedot.docId(oo), data = oo, upsert = upsert)})

  def syncAction(perustiedot: OpiskeluoikeudenPerustiedot, upsert: Boolean): FixedSqlAction[Int, NoStream, Effect.Write] = {
    PerustiedotSync += PerustiedotSyncRow(opiskeluoikeusId = perustiedot.id, data = OpiskeluoikeudenPerustiedot.serializePerustiedot(perustiedot), upsert = upsert)
  }

  def needSyncing(limit: Int): Seq[PerustiedotSyncRow] =
    runDbSync(PerustiedotSync.sortBy(_.id).take(limit).result)

  def delete(ids: Seq[Int]): Int =
    runDbSync(PerustiedotSync.filter(_.id inSetBind ids).delete)
}


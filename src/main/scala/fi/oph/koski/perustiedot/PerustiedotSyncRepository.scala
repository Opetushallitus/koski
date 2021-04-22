package fi.oph.koski.perustiedot

import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{QueryMethods, PerustiedotSyncRow}
import org.json4s.JValue
import slick.dbio.Effect
import slick.sql.FixedSqlAction


class PerustiedotSyncRepository(val db: DB) extends QueryMethods {
  def addToSyncQueueRaw(opiskeluoikeudet: Seq[JValue], upsert: Boolean): Unit = {
    val rows = opiskeluoikeudet.map(oo => PerustiedotSyncRow(
      opiskeluoikeusId = OpiskeluoikeudenPerustiedot.docId(oo),
      data = oo,
      upsert = upsert
    ))
    runDbSync(PerustiedotSync ++= rows)
  }

  def addToSyncQueue(perustiedot: OpiskeluoikeudenPerustiedot, upsert: Boolean): FixedSqlAction[Int, NoStream, Effect.Write] = {
    PerustiedotSync += PerustiedotSyncRow(
      opiskeluoikeusId = perustiedot.id,
      data = OpiskeluoikeudenPerustiedot.serializePerustiedot(perustiedot),
      upsert = upsert
    )
  }

  def queuedUpdates(limit: Int): Seq[PerustiedotSyncRow] =
    runDbSync(
      PerustiedotSync
        .sortBy(pt => (pt.opiskeluoikeusId, pt.aikaleima.asc))
        .take(limit)
        .result
    )

  def deleteFromQueue(ids: Seq[Int]): Int =
    runDbSync(PerustiedotSync.filter(_.id inSetBind ids).delete)
}


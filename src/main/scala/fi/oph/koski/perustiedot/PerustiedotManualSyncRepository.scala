package fi.oph.koski.perustiedot

import fi.oph.koski.db.{DB, HenkilöRow, HenkilöRowWithMasterInfo, KoskiOpiskeluoikeusRow, PerustiedotManualSyncRow, QueryMethods}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.{Henkilöt, KoskiOpiskeluOikeudet, PerustiedotManualSync}
import fi.oph.koski.henkilo.KoskiHenkilöCache
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.serializePerustiedot
import org.json4s.JValue


class PerustiedotManualSyncRepository(val db: DB, henkilöCache: KoskiHenkilöCache) extends QueryMethods {
  private def manualSyncRows(limit: Int): Seq[PerustiedotManualSyncRow] =
    runDbSync(
      PerustiedotManualSync
        .sortBy(pt => (pt.opiskeluoikeusOid, pt.aikaleima.asc))
        .take(limit)
        .result
    )

  def getQueuedUpdates(limit: Int) = {
    val queued = manualSyncRows(limit)
    val query = for {
      ((opiskeluoikeusRow, henkiloRow), perustiedotSyncRow) <- KoskiOpiskeluOikeudet.filter(_.oid inSetBind queued.map(_.opiskeluoikeusOid)) join Henkilöt on (_.oppijaOid === _.oid) join PerustiedotManualSync on(_._1.oid === _.opiskeluoikeusOid)
    } yield ((opiskeluoikeusRow, henkiloRow), perustiedotSyncRow)
    runDbSync(query.result)
  }

  def makeSyncRow(dbOpiskeluoikeusRow: KoskiOpiskeluoikeusRow, dbHenkilöRow: HenkilöRow): Option[JValue] = {
    runDbSync(henkilöCache.getCachedAction(dbHenkilöRow.oid)) match {
      case Some(HenkilöRowWithMasterInfo(henkilöRow, masterHenkilöRow)) =>
        Some(
          serializePerustiedot(
            OpiskeluoikeudenPerustiedot.makePerustiedot(
              dbOpiskeluoikeusRow.id,
              dbOpiskeluoikeusRow.toOpiskeluoikeusUnsafe(KoskiSpecificSession.untrustedUser),
              henkilöRow,
              masterHenkilöRow
            )
          )
        )
      case None => None
    }
  }

  def deleteFromQueue(ids: Seq[Int]): Int =
    runDbSync(PerustiedotManualSync.filter(_.id inSetBind ids).delete)
}

package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}

class PerustiedotSyncRepository(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  def add(opiskeluoikeudet: Seq[Int]): Option[Int] =
    runDbSync(PerustiedotSync ++= opiskeluoikeudet.map(PerustiedotSyncRow(_)))

  def get: Seq[PerustiedotSyncRow] = runDbSync(PerustiedotSync.result)

  def delete(ids: Seq[Int]): Int =
    runDbSync(PerustiedotSync.filter(_.id inSetBind ids).delete)
}


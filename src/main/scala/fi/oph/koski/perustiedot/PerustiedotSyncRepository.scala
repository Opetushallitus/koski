package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}

class PerustiedotSyncRepository(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  def needSyncing(opiskeluoikeudet: Seq[OpiskeluoikeudenOsittaisetTiedot]): Option[Int] = {
   runDbSync(PerustiedotSync ++= opiskeluoikeudet.map(_.id).map(PerustiedotSyncRow(_, 1)))
  }
}


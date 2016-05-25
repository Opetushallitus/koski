package fi.oph.koski.db

import java.sql.SQLException

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.log.Logging
import slick.dbio
import slick.dbio.{Effect, NoStream}
import slick.jdbc.TransactionIsolation

trait SerializableTransactions extends Logging with Futures with GlobalExecutionContext {
  def doInIsolatedTransaction[E <: Effect, Result, S <: NoStream](db: DB, action: dbio.DBIOAction[Result, S, E], description: String) = {
    val withTransactionIsolation = action.transactionally.withTransactionIsolation(TransactionIsolation.Serializable)
    /*
    1. transactionally = if any part fails, rollback everything. For example, if new version cannot be written to history table, insert/update must be rolled back.
    2. withTransactionIsolation(Serializable) = another concurrent transaction must not be allowed for the same row. otherwise, version history would be messed up.

    This mechanism is tested with Gatling simulation "UpdateSimulation" which causes concurrent updates to the same row.
    */
    def tryDo(iteration: Int): Result = {
      try {
        await(db.run(withTransactionIsolation))
      } catch {
        case e:SQLException if e.getSQLState == "40001" =>
          // PostgreSQL throws this when optimistic locking fails. Retry is the thing to do here.
          val waitTime = iteration * iteration // quadratic backoff time
          logger.warn(description + " epäonnistui samanaikaisten muutoksien vuoksi. Yritetään uudelleen " + waitTime + " millisekunnin päästä.")
          Thread.sleep(waitTime)
          tryDo(iteration + 1)
      }
    }
    tryDo(1)
  }
}

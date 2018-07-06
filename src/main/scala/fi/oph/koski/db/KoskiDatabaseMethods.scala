package fi.oph.koski.db

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.executors.Pools
import fi.oph.koski.util.{Futures, ReactiveStreamsToRx}
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.Query

trait KoskiDatabaseMethods {
  protected def db: DB

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing], skipCheck: Boolean = false): R = {
    if (!skipCheck && Thread.currentThread().getName.startsWith(Pools.databasePoolName)) {
      throw new RuntimeException("Nested transaction detected! Don't call runDbSync in a nested manner, as it will cause deadlocks.")
    }
    Futures.await(db.run(a))
  }

  def streamingQuery[E, U, C[_]](query: Query[E, U, C]) = {
    import fi.oph.koski.util.ReactiveStreamsToRx._
    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount
  }
}

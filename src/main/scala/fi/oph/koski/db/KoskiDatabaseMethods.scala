package fi.oph.koski.db

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.executors.Pools
import fi.oph.koski.util.Futures
import fi.oph.koski.util.ReactiveStreamsToRx.publisherToObservable
import rx.lang.scala.Observable
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.lifted.Query
import slick.sql.SqlStreamingAction

import scala.concurrent.duration.{Duration, _}
import scala.language.higherKinds

trait KoskiDatabaseMethods {
  protected def db: DB

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing], skipCheck: Boolean = false, timeout: Duration = 60.seconds): R = {
    if (!skipCheck && Thread.currentThread().getName.startsWith(Pools.databasePoolName)) {
      throw new RuntimeException("Nested transaction detected! Don't call runDbSync in a nested manner, as it will cause deadlocks.")
    }
    Futures.await(db.run(a), atMost = timeout)
  }

  def streamQuery[E, U, C[_]](query: Query[E, U, C]): Observable[U] = {
    streamAction(query.result)
  }

  def streamAction[E, U, C[_]](result: SqlStreamingAction[C[U], U, Effect.Read]): Observable[U] = {
    publisherToObservable(db.stream(result.transactionally.withStatementParameters(fetchSize = 1000))).publish.refCount
  }
}

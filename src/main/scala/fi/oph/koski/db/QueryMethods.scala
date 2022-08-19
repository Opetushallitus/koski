package fi.oph.koski.db

import java.sql.Date
import java.time.LocalDate
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.executors.Pools
import fi.oph.koski.util.{Futures, Retry}
import fi.oph.koski.util.ReactiveStreamsToRx.publisherToObservable
import rx.lang.scala.Observable
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.SetParameter
import slick.lifted.Query

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.higherKinds


trait DatabaseConverters {
  implicit val setLocalDate: SetParameter[LocalDate] = (localDate, params) => params.setDate(Date.valueOf(localDate))
}

trait QueryMethods extends DatabaseConverters {
  protected def db: DB

  def defaultRetryIntervalMs: Long = 1000

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing], allowNestedTransactions: Boolean = false, timeout: Duration = 60.seconds): R = {
    if (!allowNestedTransactions && Thread.currentThread().getName.startsWith(Pools.databasePoolName)) {
      throw new RuntimeException("Nested transaction detected! Don't call runDbSync in a nested manner, as it will cause deadlocks.")
    }
    Futures.await(db.run(a), atMost = timeout)
  }

  def streamingQuery[E, U, C[_]](query: Query[E, U, C]): Observable[U] = {
    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    publisherToObservable(db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000))).publish.refCount
  }

  def retryDbSync[R](
    a: DBIOAction[R, NoStream, Nothing],
    allowNestedTransactions: Boolean = false,
    timeout: Duration = 60.seconds,
    maxCount: Int = 10,
    intervalMs: Long = defaultRetryIntervalMs,
  ): R = {
    Retry.retryWithInterval(maxCount, intervalMs) {
      runDbSync(a, allowNestedTransactions, timeout)
    }
  }
}

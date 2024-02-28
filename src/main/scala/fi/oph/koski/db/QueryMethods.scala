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

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing], allowNestedTransactions: Boolean = false, timeout: Duration = 60.seconds): R =
    QueryMethods.runDbSync(db, a, allowNestedTransactions, timeout)

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

  def getSchemaHash(schema: String): String =
    runDbSync(sql"""
      SELECT md5(array_agg(t2)::text)
      FROM (
        SELECT row_to_json(t)
        FROM (
          SELECT *
          FROM information_schema.columns
          WHERE table_schema = $schema
          ORDER BY table_name, ordinal_position
        ) t
      ) t2
   """.as[String]).head
}

object QueryMethods {
  def runDbSync[R](db: DB, a: DBIOAction[R, NoStream, Nothing], allowNestedTransactions: Boolean = false, timeout: Duration = 60.seconds): R = {
    if (!allowNestedTransactions && Thread.currentThread().getName.startsWith(Pools.databasePoolName)) {
      throw new RuntimeException("Nested transaction detected! Don't call runDbSync in a nested manner, as it will cause deadlocks.")
    }
    Futures.await(db.run(a), atMost = timeout)
  }
}

package fi.oph.koski.db

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.util.{Futures, PaginationSettings, QueryPagination, ReactiveStreamsToRx}
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.Query
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

trait KoskiDatabaseMethods {
  protected def db: DB

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing]): R = Futures.await(db.run(a))

  def streamingQuery[E, U, C[_]](query: Query[E, U, C]) = {
    import ReactiveStreamsToRx._
    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    db.stream(query.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount
  }
}

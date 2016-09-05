package fi.oph.koski.db

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.util.Futures
import slick.dbio.{DBIOAction, NoStream}

trait KoskiDatabaseMethods {
  protected def db: DB

  def runDbSync[R](a: DBIOAction[R, NoStream, Nothing]): R = Futures.await(db.run(a))
}

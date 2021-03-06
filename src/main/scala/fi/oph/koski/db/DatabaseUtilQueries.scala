package fi.oph.koski.db

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.util.RegexUtils.StringWithRegex
import org.postgresql.util.PSQLException
import slick.dbio.{DBIOAction, NoStream}

import scala.concurrent.duration.DurationInt

object DatabaseUtilQueries {
  type SizeQuery = DBIOAction[Int, NoStream, Nothing]
}

class DatabaseUtilQueries(
  protected val db: DB,
  sizeQuery: DatabaseUtilQueries.SizeQuery,
  smallDatabaseMaxRows: Int
) extends QueryMethods {
  def databaseIsOnline: Boolean = {
    runDbSync(sql"select true".as[Boolean], timeout = 5.seconds).head
  }

  def databaseIsLarge: Boolean = {
    try {
      val count: Int = runDbSync(sizeQuery)
      count > smallDatabaseMaxRows
    } catch {
      case e: PSQLException if e.getMessage =~ """relation "\w+" does not exist""".r =>
        false // Allow for an uninitialized db
    }
  }
}

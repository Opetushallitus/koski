package fi.oph.koski.tiedonsiirto

import java.sql.SQLException

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OppilaitosIPOsoite
import fi.oph.koski.db.{KoskiDatabaseMethods, OppilaitosIPOsoiteRow}
import fi.oph.koski.log.Logging

class IPService(val db: DB) extends KoskiDatabaseMethods with Logging {
  def setIP(username: String, ip: String): Unit = try {
    runDbSync(OppilaitosIPOsoite.insertOrUpdate(OppilaitosIPOsoiteRow(username, ip)))
  } catch {
    case e:SQLException if e.getSQLState == "23505" =>
      logger.info("Inserting new ip address caused duplicate key exception, ignoring.")
  }

  def getIP(username: String): String =
    runDbSync(OppilaitosIPOsoite.filter(_.username === username).map(_.ip).result.headOption).getOrElse("")
}

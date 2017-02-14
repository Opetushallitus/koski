package fi.oph.koski.tiedonsiirto

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{KoskiDatabaseMethods, OppilaitosIPOsoiteRow, Tables}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OppilaitosIPOsoite

class IPService(val db: DB) extends KoskiDatabaseMethods {
  def setIP(username: String, ip: String): Unit =
    runDbSync(OppilaitosIPOsoite.insertOrUpdate(OppilaitosIPOsoiteRow(username, ip)))

  def getIP(username: String): String =
    runDbSync(OppilaitosIPOsoite.filter(_.username === username).map(_.ip).result.headOption).getOrElse("")
}

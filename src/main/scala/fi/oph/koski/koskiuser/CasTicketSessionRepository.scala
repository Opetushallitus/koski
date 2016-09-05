package fi.oph.koski.koskiuser
import java.sql.Timestamp

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{CasServiceTicketSessionRow, KoskiDatabaseMethods, GlobalExecutionContext, Tables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import slick.dbio.{DBIOAction, NoStream}

import scala.concurrent.Future

class CasTicketSessionRepository(val db: DB, sessionTimeout: SessionTimeout) extends KoskiDatabaseMethods with GlobalExecutionContext with Timing with Logging {
  private def now = new Timestamp(System.currentTimeMillis())

  def store(ticket: String, user: AuthenticationUser) = {
    runDbSync((Tables.CasServiceTicketSessions += CasServiceTicketSessionRow(ticket, user.name, user.oid, now, now)))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = timed("getUserByTicket", 0) {
    val limit = new Timestamp(System.currentTimeMillis() - sessionTimeout.milliseconds)
    val query = Tables.CasServiceTicketSessions.filter(row => row.serviceTicket === ticket && row.updated >= limit)

    runDbSync(query.map(_.updated).update(now)) // update the "updated" timestamp each time

    runDbSync(query.result).map(row => AuthenticationUser(row.userOid, row.username, Some(ticket))).headOption
  }

  def removeSessionByTicket(ticket: String) = {
    val query = Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket)
    val deleted = runDbSync(query.delete)
    deleted match {
      case 1 =>
        logger.info(s"Invalidated session for ticket $ticket")
      case 0 =>
      case n =>
        logger.error("Multiple sessions deleted for ticket $ticket")
    }
  }
}

package fi.oph.koski.koskiuser
import java.sql.Timestamp

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{CasServiceTicketSessionRow, Futures, GlobalExecutionContext, Tables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing

class CasTicketSessionRepository(db: DB) extends Futures with GlobalExecutionContext with Timing with Logging {
  val ticketExpirationSeconds = 600
  // TODO: timestamp for cleaning up even if cas never calls remove URL

  private def now = new Timestamp(System.currentTimeMillis())

  def store(ticket: String, user: AuthenticationUser) = {
    db.run((Tables.CasServiceTicketSessions += CasServiceTicketSessionRow(ticket, user.name, user.oid, now, now)))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = timed("getUserByTicket", 0) {
    val limit = new Timestamp(System.currentTimeMillis() - ticketExpirationSeconds * 1000)

    val query = Tables.CasServiceTicketSessions.filter(row => row.serviceTicket === ticket && row.updated >= limit)

    db.run(query.map(_.updated).update(now))

    await(db.run(query.result)).map(row => AuthenticationUser(row.userOid, row.username, Some(ticket))).headOption
  }

  def removeSessionByTicket(ticket: String) = {
    val query = Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket)
    val deleted = await(db.run(query.delete))
    deleted match {
      case 1 =>
        logger.info(s"Invalidated session for ticket $ticket")
      case 0 =>
      case n =>
        logger.error("Multiple sessions deleted for ticket $ticket")
    }
  }
}

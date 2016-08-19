package fi.oph.koski.koskiuser
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{CasServiceTicketSessionRow, Futures, GlobalExecutionContext, Tables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing

class CasTicketSessionRepository(db: DB) extends Futures with GlobalExecutionContext with Timing with Logging {
  // TODO: timestamp for cleaning up even if cas never calls remove URL

  def store(sessionId: String, ticket: String, user: AuthenticationUser) = {
    db.run((Tables.CasServiceTicketSessions += CasServiceTicketSessionRow(ticket, sessionId, user.name, user.oid)))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = timed("getUserByTicket", 0) {
    await(db.run(Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket).result)).map(row => AuthenticationUser(row.userOid, row.username, Some(ticket))).headOption
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

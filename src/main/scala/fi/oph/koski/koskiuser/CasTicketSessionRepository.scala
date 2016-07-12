package fi.oph.koski.koskiuser
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{CasServiceTicketSessionRow, Futures, GlobalExecutionContext, Tables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

class CasTicketSessionRepository(db: DB) extends Futures with GlobalExecutionContext {
  def store(sessionId: String, ticket: String, user: AuthenticationUser) = {
    db.run((Tables.CasServiceTicketSessions += CasServiceTicketSessionRow(ticket, sessionId, user.name, user.oid)))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = {
    await(db.run(Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket).result)).map(row => AuthenticationUser(row.userOid, row.username, Some(ticket))).headOption
  }

  def removeSessionByTicket(ticket: String): Option[String] = {
    val query = Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket)
    await(db.run(for {
      result <- query.map(_.sessionId).result
      _ <- query.delete
    } yield {
      result
    })).headOption
  }
}

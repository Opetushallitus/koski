package fi.oph.koski.koskiuser
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{CasServiceTicketSessionRow, Futures, GlobalExecutionContext, Tables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

class CasTicketSessionRepository(db: DB) extends Futures with GlobalExecutionContext {

  def store(sessionId: String, ticket: String) = synchronized {
    db.run((Tables.CasServiceTicketSessions += CasServiceTicketSessionRow(ticket, sessionId)))
  }

  def getSessionIdByTicket(ticket: String): Option[String] = synchronized {
    val query = Tables.CasServiceTicketSessions.filter(_.serviceTicket === ticket)
    await(db.run(for {
      result <- query.map(_.sessionId).result
      _ <- query.delete
    } yield {
      result
    })).headOption
  }
}

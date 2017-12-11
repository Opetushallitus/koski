package fi.oph.koski.sso

import java.sql.Timestamp

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, SSOSessionRow, Tables}
import fi.oph.koski.koskiuser.{AuthenticationUser, SessionTimeout}
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.util.Timing

class KoskiSessionRepository(val db: DB, sessionTimeout: SessionTimeout) extends KoskiDatabaseMethods with GlobalExecutionContext with Timing with Logging {
  private def now = new Timestamp(System.currentTimeMillis())

  def store(ticket: String, user: AuthenticationUser, clientIp: String) = {
    AuditLog.log(AuditLogMessage(KoskiOperation.LOGIN, user, clientIp, Map()))
    runDbSync(Tables.CasServiceTicketSessions += SSOSessionRow(ticket, user.username, user.oid, user.name, now, now))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = timed("getUserByTicket", 10) {
    val limit = new Timestamp(System.currentTimeMillis() - sessionTimeout.milliseconds)
    val query = Tables.CasServiceTicketSessions.filter(row => row.serviceTicket === ticket && row.updated >= limit)

    val action = for {
      _ <- query.map(_.updated).update(now)
      result <- query.result
    } yield {
      result
    }

    runDbSync(action).map { row =>
      AuthenticationUser(row.userOid, row.username, row.name, Some(ticket))
    }.headOption
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

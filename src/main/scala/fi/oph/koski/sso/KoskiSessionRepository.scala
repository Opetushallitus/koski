package fi.oph.koski.sso

import java.net.InetAddress
import java.sql.Timestamp
import java.time.Instant

import fi.oph.koski.db.{DB, DatabaseExecutionContext, QueryMethods, SSOSessionRow, KoskiTables}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.huoltaja.HuollettavatSearchResult
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AuthenticationUser, SessionTimeout}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.util.Timing
import org.json4s.JsonAST.JValue

class KoskiSessionRepository(val db: DB, sessionTimeout: SessionTimeout) extends QueryMethods with DatabaseExecutionContext with Timing with Logging {

  def store(ticket: String, user: AuthenticationUser, clientIp: InetAddress, userAgent: String) = {
    val operation = if (user.kansalainen) KoskiOperation.KANSALAINEN_LOGIN else KoskiOperation.LOGIN
    AuditLog.log(KoskiAuditLogMessage(operation, user, clientIp, ticket, userAgent))
    runDbSync(KoskiTables.CasServiceTicketSessions += SSOSessionRow(ticket, user.username, user.oid, user.name, now, now, user.huollettavat.map(serialize)))
  }

  def getUserByTicket(ticket: String): Option[AuthenticationUser] = timed("getUserByTicket", 10) {
    val limit = new Timestamp(System.currentTimeMillis() - sessionTimeout.milliseconds)
    val query = KoskiTables.CasServiceTicketSessions.filter(row => row.serviceTicket === ticket && row.updated >= limit)

    val action = for {
      _ <- query.map(_.updated).update(now)
      result <- query.result
    } yield {
      result
    }

    runDbSync(action).map { row =>
      AuthenticationUser(row.userOid, row.username, row.name, Some(ticket), huollettavat = row.huollettavatSearchResult.map(deserialize))
    }.headOption
  }

  def removeSessionByTicket(ticket: String) = {
    val query = KoskiTables.CasServiceTicketSessions.filter(_.serviceTicket === ticket)
    val deleted = runDbSync(query.delete)
    deleted match {
      case 1 =>
        logger.info(s"Invalidated session for ticket $ticket")
      case 0 =>
      case n =>
        logger.error(s"Multiple sessions deleted for ticket $ticket")
    }
  }

  def removeSessionByUsername(username: String) = {
    val query = KoskiTables.CasServiceTicketSessions.filter(_.username === username)
    runDbSync(query.delete)
  }

  def purgeOldSessions(before: Instant): Unit = {
    val timestamp = new Timestamp(before.toEpochMilli)
    val query = KoskiTables.CasServiceTicketSessions.filter(_.updated < timestamp)
    val deleted = runDbSync(query.delete)
    logger.info(s"Purged $deleted sessions older than $before")
  }

  private def now = new Timestamp(System.currentTimeMillis())
  private def deserialize(huoltajaSearchResult: JValue) = JsonSerializer.extract[HuollettavatSearchResult](huoltajaSearchResult)
  private def serialize(searchResult: HuollettavatSearchResult) = JsonSerializer.serializeWithRoot(searchResult)
}

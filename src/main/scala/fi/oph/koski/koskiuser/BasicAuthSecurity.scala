package fi.oph.koski.koskiuser

import java.sql.Timestamp
import java.sql.Timestamp.{valueOf => timestamp}
import java.time.{Clock, Duration, LocalDateTime}

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.Tables.FailedLoginAttempt
import fi.oph.koski.db.{KoskiDatabaseMethods, _}
import fi.oph.koski.koskiuser.IgnoredPostgresErrors.{duplicateKey, lockNotAvailable, transactionAborted}
import fi.oph.koski.log.Logging
import org.postgresql.util.PSQLException
import slick.dbio.Effect.Write
import slick.jdbc.GetResult
import slick.sql.SqlAction

class BasicAuthSecurity(val db: DB, config: Config, clock: Clock = Clock.systemDefaultZone) extends DatabaseExecutionContext with KoskiDatabaseMethods with Logging {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  private val initialDelay: Duration = config.getDuration("authenticationFailed.initialDelay")
  private val resetDuration: Duration = config.getDuration("authenticationFailed.resetAfter")

  def isBlacklisted(username: String, loginSuccessful: Boolean): Boolean = {
    try {
      checkAndUpdateBlacklist(username, loginSuccessful)
    } catch {
      case e: PSQLException if List(duplicateKey, lockNotAvailable, transactionAborted).contains(e.getSQLState) =>
        val msg = s"Got error while trying to add login block: ${e.getMessage}: ${e.getSQLState}"
        if (loginSuccessful) logger.error(e)(msg)
        else logger.warn(msg)
        // These failures occur when trying to update username's blacklist, so it's safe to assume user is blacklisted
        true
    }
  }

  private def checkAndUpdateBlacklist(username: String, loginSuccessful: Boolean) = {
    runDbSync((for {
      alreadyBlocked <- getLoginBlockedAction(username)
      blockedUntil <- alreadyBlocked match {
        case Some(block) if isBlockActive(block) =>
          logger.warn(s"Too many failed login attempts (${block.count}) for username $username, blocking login until ${blockedUntil(block)}")
          DBIO.successful(true)
        case _ =>
          if (loginSuccessful) {
            for {
              _ <-  DBIO.sequenceOption(delete(alreadyBlocked))
            } yield false
          } else {
            for {
              block <- selectBlockForUpdate(username)
              newBlock <- block match {
                case Some(b) if isBlockActive(b) => DBIO.successful(block)
                case _ => upsertLoginBlock(username, block)
              }
            } yield true
          }
      }
    } yield blockedUntil).transactionally)
  }

  private def delete(block: Option[FailedLoginAttemptRow]): Option[DBIOAction[Int, NoStream, Write]] =
    block.map(b => FailedLoginAttempt.filter(_.username === b.username).delete.asTry.map(_.toOption.getOrElse(0)))

  private def upsertLoginBlock(username: String, blocked: Option[FailedLoginAttemptRow]) = {
    for {
      newRow <- DBIO.successful(blocked.map(updatedRow).getOrElse(failedLoginAttemptRow(username)))
      _ <- DBIO.successful(logger.info(s"Upserting new login block row $newRow, ${blockedUntil(newRow)}"))
      _ <- FailedLoginAttempt.insertOrUpdate(newRow)
    } yield Some(newRow)
  }

  def getLoginBlockedAction(username: String): SqlAction[Option[FailedLoginAttemptRow], NoStream, _] =
    FailedLoginAttempt.filter(_.username === username).result.headOption

  def isBlockActive(failedLogin: FailedLoginAttemptRow): Boolean = {
    blockedUntil(failedLogin).isAfter(LocalDateTime.now(clock))
  }

  def blockedUntil(failedLogin: FailedLoginAttemptRow): LocalDateTime = {
    val resetTime: LocalDateTime = failedLogin.firstFailTime.plus(resetDuration)
    val block: LocalDateTime = failedLogin.firstFailTime.plus(initialDelay.multipliedBy(Math.pow(2, failedLogin.count - 1).toInt))
    if (block.isAfter(resetTime)) {
      resetTime
    } else {
      block
    }
  }

  implicit private val getResult = GetResult[FailedLoginAttemptRow](r => FailedLoginAttemptRow(r.<<,r.<<,r.<<))
  private def selectBlockForUpdate(username: String) =
    sql"""
      SELECT username, time, count
      FROM failed_login_attempt
      WHERE username = $username
      FOR UPDATE NOWAIT
    """.as[FailedLoginAttemptRow].headOption

  private def updatedRow(row: FailedLoginAttemptRow): FailedLoginAttemptRow = {
    val now = LocalDateTime.now(clock)
    if (row.firstFailTime.plus(resetDuration).isBefore(now)) {
      logger.info(s"Login blacklist reset for user ${row.username}")
      row.copy(count = 1, time = timestamp(now))
    } else {
      row.copy(count = row.count + 1)
    }
  }

  private def failedLoginAttemptRow(username: String) =
    FailedLoginAttemptRow(username = username, time = timestamp(LocalDateTime.now(clock)), count = 1)
}

object IgnoredPostgresErrors {
  val duplicateKey = "23505"
  val lockNotAvailable = "55P03"
  val transactionAborted = "25P02"
}

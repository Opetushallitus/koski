package fi.oph.koski.koskiuser

import java.sql.Timestamp
import java.sql.Timestamp.{valueOf => timestamp}
import java.time.LocalDateTime
import java.time.LocalDateTime.now

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{KoskiDatabaseMethods, Tables, _}

import scala.math.pow

class BasicAuthSecurity(val db: DB, config: Config) extends GlobalExecutionContext with KoskiDatabaseMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  private val initialDelay = config.getDuration("authenticationFailed.initialDelay")
  private val resetPeriod = config.getDuration("authenticationFailed.resetAfter")

  def attemptAllowed(username: String): Boolean = {
    val attempt: Option[FailedLoginAttemptRow] = runDbSync(Tables.FailedLoginAttempt.filter(r => r.username === username && r.time >= resetTime).result.headOption)
    val nextAttempt: Option[LocalDateTime] = attempt.map(a => a.time.toLocalDateTime.plus(initialDelay.multipliedBy(pow(2, a.count - 1).toInt)))
    !nextAttempt.exists(nextAllowedTime => nextAllowedTime.isAfter(now()))
  }

  def loginFailed(username: String): Unit = {
    runDbSync((for {
      row <- Tables.FailedLoginAttempt.filter(r => r.username === username).map(r => (r.time, r.count)).forUpdate.result
      _ <- if (row.nonEmpty) {
        val (time, count) = row.head
        Tables.FailedLoginAttempt.filter(_.username === username).update(FailedLoginAttemptRow(username, timestamp(now()), if (time.toLocalDateTime.isBefore(now.minusHours(24))) 1 else count + 1))
      } else {
        Tables.FailedLoginAttempt += FailedLoginAttemptRow(username, timestamp(now()), 1)
      }
    } yield ()).transactionally)
  }

  def loginSuccess(username: String): Unit =
    runDbSync(Tables.FailedLoginAttempt.filter(_.username === username).delete)

  private def resetTime = Timestamp.valueOf(now().minus(resetPeriod))
}

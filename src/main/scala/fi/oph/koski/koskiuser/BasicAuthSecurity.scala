package fi.oph.koski.koskiuser

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.LocalDateTime.now

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.{KoskiDatabaseMethods, Tables, _}

import scala.math.pow

class BasicAuthSecurity(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  val delaySeconds = 10

  def attemptAllowed(username: String): Boolean = {
    val attempt: Option[FailedLoginAttemptRow] = runDbSync(Tables.FailedLoginAttempt.filter(r => r.username === username && r.time >= `24hoursAgo`).result.headOption)
    val nextAttempt: Option[LocalDateTime] = attempt.map(a => a.time.toLocalDateTime.plusSeconds(pow(2, a.count - 1).toInt * delaySeconds))
    !nextAttempt.exists(nextAllowedTime => nextAllowedTime.isAfter(now()))
  }

  def attemptFailed(username: String): Unit = {
    runDbSync((for {
      row <- Tables.FailedLoginAttempt.filter(r => r.username === username).map(r => (r.time, r.count)).forUpdate.result
      _ <- if (row.nonEmpty) {
        val (time, count) = row.head
        Tables.FailedLoginAttempt.filter(_.username === username).update(FailedLoginAttemptRow(username, new Timestamp(System.currentTimeMillis), if (time.toLocalDateTime.isBefore(now.minusHours(24))) 1 else count + 1))
      } else {
        Tables.FailedLoginAttempt += FailedLoginAttemptRow(username, new Timestamp(System.currentTimeMillis), 1)
      }
    } yield ()).transactionally)
  }

  private def `24hoursAgo` = Timestamp.valueOf(now().minusHours(24))
}

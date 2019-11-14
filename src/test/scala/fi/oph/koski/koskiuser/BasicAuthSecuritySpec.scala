package fi.oph.koski.koskiuser

import java.time.Duration.{ofSeconds => seconds}
import java.time.ZoneId.{systemDefault => systemZone}
import java.time._
import java.time.temporal.Temporal

import com.typesafe.config.ConfigFactory
import fi.oph.koski.api.DatabaseTestMethods
import fi.oph.koski.db.{FailedLoginAttemptRow, Tables}
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

import scala.collection.JavaConverters._

class BasicAuthSecuritySpec extends FreeSpec with Matchers with BeforeAndAfterEach with DatabaseTestMethods {
  private val kalle = "kalle"
  override def afterEach = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
    runDbSync(Tables.FailedLoginAttempt.filter(_.username === kalle).delete)
  }

  "BasicAuthSecurity" - {
    "blacklists failed login" in {
      security(clockNow).isBlacklisted(kalle, loginSuccessful = false) should equal(true)
      security(clockNow).isBlacklisted(kalle, loginSuccessful = true) should equal(true)
      security(seconds(10)).isBlacklisted(kalle, loginSuccessful = true) should equal(false)
    }

    "blacklist time multiplied by two after each failed login" in {
      val resetTime = LocalDateTime.now(clockNow).plus(resetDuration)
      val initialBlock = failLogin(clockNow).blockedUntil
      val blocks = failContinually(initialBlock).takeWhile { time =>
        secondsBetween(initialBlock, time) < secondsBetween(initialBlock, resetTime)
      }.toList

      val consecutiveBlockDurations = blocks.zip(blocks.drop(1)).map { case (t1, t2) =>
        secondsBetween(t1, t2)
      }

      consecutiveBlockDurations should not be empty
      consecutiveBlockDurations.zip(consecutiveBlockDurations.drop(1)).foreach { case (d1, d2) =>
        d2 should equal (d1 * 2)
      }
    }

    "blacklist time capped to reset duration" in {
      val initialBlock = failLogin(clockNow).blockedUntil
      val block = failContinually(initialBlock).take(20).toList.last
      val resetTime = LocalDateTime.now(Clock.offset(clockNow, resetDuration))
      val difference = Math.abs(secondsBetween(block, resetTime)).toInt
      difference should be <= 1
    }

    "clears blacklist once login succeeds" in {
      security(clockNow).isBlacklisted(kalle, loginSuccessful = false) should equal(true)
      runDbSync(security().getLoginBlockedAction(kalle)) should not be empty
      security(initialDelay).isBlacklisted(kalle, loginSuccessful = true) should equal(false)
      runDbSync(security().getLoginBlockedAction(kalle)) should be(empty)
    }

    "resets fail count and time after reset period" in {
      val fail1 = failLogin(clockNow)
      fail1.row.count should equal(1)
      failLogin(fail1.blockClock).row.count should equal(2)

      val resetClock = Clock.offset(clockNow, resetDuration)
      val resetFail = failLogin(resetClock)
      resetFail.row.count should equal(1)
      val difference = Math.abs(secondsBetween(resetFail.row.localTime, LocalDateTime.now(resetClock))).toInt
      difference should be <= 1
    }
  }

  private def failContinually(initialBlock: LocalDateTime) =
    Iterator.iterate(initialBlock) { blockedUntil =>
      failLogin(clockFromDateTime(blockedUntil)).blockedUntil
    }

  private def failLogin(clock: Clock) = {
    security(clock).isBlacklisted(kalle, loginSuccessful = false)
    Fail(runDbSync(security(clockNow).getLoginBlockedAction(kalle)).get)
  }

  private val config = ConfigFactory.parseMap(Map("authenticationFailed.initialDelay" -> "10s", "authenticationFailed.resetAfter" -> "24h").asJava)
  private val resetDuration: Duration = config.getDuration( "authenticationFailed.resetAfter" )
  private val initialDelay: Duration = config.getDuration( "authenticationFailed.initialDelay" )
  private val clockNow = Clock.systemDefaultZone

  private def secondsBetween(t1: Temporal, t2: Temporal) = Duration.between(t1, t2).toMillis / 1000
  private def security(offset: Duration) = new BasicAuthSecurity(db, config, Clock.offset(clockNow, offset))
  private def security(clock: Clock = clockNow) = new BasicAuthSecurity(db, config, clock)
  private def clockFromDateTime(blockedUntil: LocalDateTime) =
    Clock.fixed(blockedUntil.atZone(systemZone).toInstant, systemZone)

  case class Fail(row: FailedLoginAttemptRow) {
    def blockedUntil = security().blockedUntil(row)
    def blockClock = clockFromDateTime(security().blockedUntil(row))
  }
}


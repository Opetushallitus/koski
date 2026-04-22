package fi.oph.koski.sso

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{KoskiTables, SSOSessionRow}
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, TestEnvironment}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.time.{Instant, ZonedDateTime}
import java.time.temporal.ChronoUnit
import java.util.UUID

class SessionRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers with DatabaseTestMethods with BeforeAndAfterAll {
  private def createDummySession(dateTime: ZonedDateTime) = {
    val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
    val sqlTimestamp = Timestamp.from(dateTime.toInstant.truncatedTo(ChronoUnit.MICROS))
    runDbSync(KoskiTables.CasServiceTicketSessions += SSOSessionRow(
      fakeServiceTicket, "test", "test", "test", sqlTimestamp, sqlTimestamp, None)
    )
  }

  private def sessionsStarteds = {
    val query = KoskiTables.CasServiceTicketSessions.map(_.started)
    runDbSync(query.result)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    runDbSync(KoskiTables.CasServiceTicketSessions.delete)
  }

  "Vanhentuneiden sessioiden poisto" in {
    val staleSessions = Vector(
      ZonedDateTime.now().minusMonths(22),
      ZonedDateTime.now().minusMonths(13)
    )
    val currentSessions = Vector(
      ZonedDateTime.now().minusMonths(11),
      ZonedDateTime.now().minusMonths(1)
    )
    (staleSessions ++ currentSessions).foreach(createDummySession)
    sessionsStarteds.length should be(4)

    val purgeBefore = ZonedDateTime.now().minusYears(1).toInstant
    KoskiApplicationForTests.koskiSessionRepository.purgeOldSessions(purgeBefore)

    sessionsStarteds.map(_.toInstant) should contain theSameElementsAs(
      currentSessions.map(_.toInstant.truncatedTo(ChronoUnit.MICROS))
    )
  }
}

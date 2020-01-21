package fi.oph.koski.sso

import java.sql.Timestamp
import java.time.ZonedDateTime
import java.util.UUID

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.DatabaseTestMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{SSOSessionRow, Tables}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class KoskiSessionRepositorySpec extends FreeSpec with Matchers with DatabaseTestMethods with BeforeAndAfterAll {
  private def createDummySession(dateTime: ZonedDateTime) = {
    val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
    val sqlTimestamp = new Timestamp(dateTime.toInstant.toEpochMilli)
    runDbSync(Tables.CasServiceTicketSessions += SSOSessionRow(
      fakeServiceTicket, "test", "test", "test", sqlTimestamp, sqlTimestamp)
    )
  }

  private def sessionsStarteds = {
    val query = Tables.CasServiceTicketSessions.map(_.started)
    runDbSync(query.result)
  }

  override protected def beforeAll(): Unit = {
    runDbSync(Tables.CasServiceTicketSessions.delete)
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
      currentSessions.map(_.toInstant)
    )
  }
}

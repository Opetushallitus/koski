package fi.oph.koski.sso

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{KoskiTables, SSOSessionRow}
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, TestEnvironment}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.time.{Instant, ZonedDateTime}
import java.util.UUID

class SessionRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers with DatabaseTestMethods with BeforeAndAfterAll {
  // Returns the `started` instant as the DB actually stored it (not as we sent it).
  private def insertSession(at: ZonedDateTime): Instant = {
    val serviceTicket = "koski-" + UUID.randomUUID()
    val ts = Timestamp.from(at.toInstant)
    runDbSync(KoskiTables.CasServiceTicketSessions += SSOSessionRow(
      serviceTicket = serviceTicket,
      username = "test",
      userOid = "test",
      name = "test",
      started = ts,
      updated = ts,
      huollettavatSearchResult = None,
    ))
    runDbSync(
      KoskiTables.CasServiceTicketSessions.filter(_.serviceTicket === serviceTicket).map(_.started).result.head
    ).toInstant
  }

  private def storedSessionInstants: Seq[Instant] =
    runDbSync(KoskiTables.CasServiceTicketSessions.map(_.started).result).map(_.toInstant)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    runDbSync(KoskiTables.CasServiceTicketSessions.delete)
  }

  "Vanhentuneiden sessioiden poisto" in {
    val now = ZonedDateTime.now()
    val cutoff = now.minusYears(1).toInstant

    Seq(now.minusMonths(22), now.minusMonths(13)).foreach(insertSession)
    val currentStarts = Seq(now.minusMonths(11), now.minusMonths(1)).map(insertSession)
    storedSessionInstants should have size 4

    KoskiApplicationForTests.koskiSessionRepository.purgeOldSessions(cutoff)

    storedSessionInstants should contain theSameElementsAs currentStarts
  }
}

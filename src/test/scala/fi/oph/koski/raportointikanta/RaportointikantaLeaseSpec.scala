package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.TestEnvironment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration

class RaportointikantaLeaseSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterEach {
  private val app = KoskiApplicationForTests
  private val db = app.masterDatabase.db

  override protected def beforeEach(): Unit = {
    QueryMethods.runDbSync(db, sql"DELETE FROM worker_lease".asUpdate)
    super.beforeEach()
  }

  "Raportointikanta loading respects existing lease" in {
    app.workerLeaseRepository.tryAcquireOrRenew("raportointikanta-loader", 1, "other-worker", Duration.ofSeconds(30)) should be(true)

    val service = new RaportointikantaService(app)
    val started = service.loadRaportointikanta(force = true, onEnd = () => ())

    started should be(false)
    app.workerLeaseRepository.activeHolders("raportointikanta-loader") should contain("other-worker")
  }
}

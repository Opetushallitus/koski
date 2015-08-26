package fi.oph.tor.perftest

import fi.oph.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.db.{DatabaseConfig, Futures, GlobalExecutionContext, TorDatabase}
import fi.oph.tor.model.Identified.Id
import fi.oph.tor.model.Suoritus
import fi.vm.sade.utils.Timer
import fi.vm.sade.utils.slf4j.Logging
import slick.dbio.DBIO
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration._

object PerfTestDataGenerator extends App with Logging with Futures with GlobalExecutionContext {
  val tutkintorakennePerOppija = List(("tutkinto", 2), ("tutkinnon-osa", 5), ("kurssi", 4))
  private val suoritukset: Iterator[Suoritus] = TestDataGenerator.generoiSuorituksia(oppijoita = 1000000, organisaatioita = 100, tutkintorakennePerOppija)
  
  val db = TorDatabase.init(DatabaseConfig.localDatabase).db
  val tor = new TodennetunOsaamisenRekisteri(db)

  logger.info("Starting performance test data insertion")
  Timer.timed("Data insertion") {
    val transactionSize = 500
    val parallelism = 10

    val transactionFutures: Iterator[Future[Iterable[Id]]] = suoritukset.sliding(transactionSize, transactionSize).map { slice =>
      db.run(DBIO.sequence(slice.map(tor.insertSuoritusAction(_))).transactionally)
    }

    val syncBlocks: Iterator[Future[Iterable[Iterable[Id]]]] = transactionFutures.sliding(parallelism, parallelism).map(Future.sequence(_))

    syncBlocks.zipWithIndex.foreach { case (block, i) =>
      Timer.timed("Block " + i) {
        await(block, 300 seconds)
      }
    }
  }
  logger.info("Complete!")
}
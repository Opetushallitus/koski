package fi.oph.tor.perftest

import fi.oph.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.db.{DatabaseConfig, Futures, GlobalExecutionContext, TorDatabase}
import fi.oph.tor.model.Suoritus
import fi.vm.sade.utils.slf4j.Logging

object PerfTestDataGenerator extends App with Logging with Futures {
  val tutkintorakennePerOppija = List(("tutkinto", 2), ("tutkinnon-osa", 5), ("kurssi", 4))
  private val suoritukset: Seq[Suoritus] = TestDataGenerator.generoiSuorituksia(oppijoita = 100, organisaatioita = 100, tutkintorakennePerOppija)
  
  implicit val executionContext = GlobalExecutionContext.context
  val db = TorDatabase.forConfig(DatabaseConfig.localPostgresDatabase)
  val tor = new TodennetunOsaamisenRekisteri(db)
  logger.info("Starting performance test data generation")

  ConsoleProgressBar.showProgress(suoritukset) { suoritus =>
    await(tor.insertSuoritus(suoritus))
  }

  logger.info("Finished performance test data generation")
}



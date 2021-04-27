package fi.oph.koski.valpas.db

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.valpasrepository.{ValpasExampleData, ValpasKuntailmoitusQueryService}

class ValpasDatabaseFixtureLoader(app: KoskiApplication) extends Logging {
  private val kuntailmoitusQueryService = new ValpasKuntailmoitusQueryService(app)

  def reset(): Unit = {
    logger.info("Resetting Valpas DB fixtures")
    kuntailmoitusQueryService.truncate()
    loadIlmoitukset()
  }

  private def loadIlmoitukset(): Unit = {
    val fixtures = ValpasExampleData.ilmoitukset
    logger.info(s"Inserting ${fixtures.length} ilmoitus fixtures")
    fixtures.foreach { fx =>
      kuntailmoitusQueryService.create(fx).left.foreach(e => logger.error(s"Fixture insertion failed: $e"))
    }
  }
}

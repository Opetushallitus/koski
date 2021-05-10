package fi.oph.koski.valpas.db

import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.valpasrepository.{ValpasExampleData, ValpasKuntailmoitusRepository}

class ValpasDatabaseFixtureLoader(kuntailmoitusRepository: ValpasKuntailmoitusRepository) extends Logging {
  def reset(): Unit = {
    logger.info("Resetting Valpas DB fixtures")
    kuntailmoitusRepository.truncate()
    loadIlmoitukset()
  }

  private def loadIlmoitukset(): Unit = {
    val fixtures = ValpasExampleData.ilmoitukset
    logger.info(s"Inserting ${fixtures.length} ilmoitus fixtures")
    fixtures.foreach { fx =>
      kuntailmoitusRepository.create(fx).left.foreach(e => logger.error(s"Fixture insertion failed: $e"))
    }
  }
}

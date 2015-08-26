package fi.oph.tor.db

import fi.oph.tor.db.Tables._
import fi.vm.sade.utils.slf4j.Logging
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

object DatabaseTestFixture extends Logging {
  def clear = {
    DBIO.seq(Koodistoviite.delete, Arviointi.delete, Suoritus.delete, Komoto.delete)
  }
}


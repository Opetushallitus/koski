package fi.oph.tor.db

import fi.oph.tor.log.Logging
import slick.dbio.DBIO

object DatabaseTestFixture extends Logging {
  def clear = {
    DBIO.seq()
  }
}


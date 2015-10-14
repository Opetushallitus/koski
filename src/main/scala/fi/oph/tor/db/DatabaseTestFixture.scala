package fi.oph.tor.db

import fi.vm.sade.utils.slf4j.Logging
import slick.dbio.DBIO

object DatabaseTestFixture extends Logging {
  def clear = {
    DBIO.seq()
  }
}


package fi.oph.tor.fixture

import com.typesafe.config.Config
import fi.oph.tor.db.TorDatabase._

object Fixtures {
  def shouldUseFixtures(config: Config) = {
    if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      config.isLocal
    }
  }
}

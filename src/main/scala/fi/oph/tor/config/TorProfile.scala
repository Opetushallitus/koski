package fi.oph.tor.config

import fi.oph.tor.db._

object TorProfile {
  lazy val fromSystemProperty = fromString(System.getProperty("tor.profile", "default"))

  def fromString(profile: String) = profile match {
    case "default" => new Default()
    case "it" => new IntegrationTest()
    case "cloud" => new Cloud()
  }
}

trait TorProfile {
  def database: TorDatabase
}

class Default extends TorProfile with GlobalExecutionContext {
  lazy val database = TorDatabase.init(DatabaseConfig.localDatabase)
}

class IntegrationTest extends TorProfile with Futures with GlobalExecutionContext {
  lazy val database = {
    val database: TorDatabase = TorDatabase.init(DatabaseConfig.localTestDatabase)
    await(database.db.run(DatabaseTestFixture.clear))
    database
  }
}

class Cloud extends TorProfile with GlobalExecutionContext {
  lazy val database = TorDatabase.remoteDatabase(DatabaseConfig.cloudDatabase)
}

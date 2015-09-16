package fi.oph.tor.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.tor.db._
import fi.oph.tor.security.Authentication
import fi.vm.sade.security.ldap.DirectoryClient

object TorProfile {
  lazy val fromSystemProperty = fromString(System.getProperty("tor.profile", "local"))

  def fromString(profile: String) = profile match {
    case "local" => new Local()
    case "it" => new IntegrationTest()
    case "cloud" => new Cloud()
  }
}

trait TorProfile {
  def database: TorDatabase
  lazy val directoryClient: DirectoryClient = Authentication.directoryClient(config)
  lazy val config: Config = ConfigFactory.load
}

class Local extends TorProfile with GlobalExecutionContext {
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
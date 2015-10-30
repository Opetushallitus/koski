package fi.oph.tor.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.db._
import fi.oph.tor.eperusteet.EPerusteetRepository
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.security.Authentication
import fi.oph.tor.opintooikeus.{OpintoOikeusRepositoryWithFixtures, PostgresOpintoOikeusRepository, OpintoOikeusRepository}
import fi.oph.tor.user.UserRepository
import fi.oph.tor.util.Timed._
import fi.oph.tor.util.{Timed, LoggingProxy}
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
  lazy val oppijaRepository = Timed.timedProxy(OppijaRepository(config))
  lazy val tutkintoRepository = new TutkintoRepository(EPerusteetRepository.apply(config))
  lazy val oppilaitosRepository = new OppilaitosRepository
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(config)
  def opintoOikeusRepository: OpintoOikeusRepository
  lazy val userRepository = UserRepository(config)
  def resetMocks = {
    oppijaRepository.resetFixtures
    opintoOikeusRepository.resetFixtures
  }
}

class Local extends TorProfile with GlobalExecutionContext {
  lazy val database = TorDatabase.init(DatabaseConfig.localDatabase)
  lazy val opintoOikeusRepository = timedProxy[OpintoOikeusRepository](new OpintoOikeusRepositoryWithFixtures(database.db))
}

class IntegrationTest extends TorProfile with GlobalExecutionContext {
  lazy val database = TorDatabase.init(DatabaseConfig.localTestDatabase)
  lazy val opintoOikeusRepository = timedProxy[OpintoOikeusRepository](new OpintoOikeusRepositoryWithFixtures(database.db))
}

class Cloud extends TorProfile with GlobalExecutionContext {
  lazy val database = TorDatabase.remoteDatabase(DatabaseConfig.cloudDatabase)
  override def resetMocks = throw new UnsupportedOperationException("Mock reset not supported")
  lazy val opintoOikeusRepository = timedProxy[OpintoOikeusRepository](new PostgresOpintoOikeusRepository(database.db))
}
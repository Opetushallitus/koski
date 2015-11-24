package fi.oph.tor.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.db._
import fi.oph.tor.eperusteet.EPerusteetRepository
import fi.oph.tor.fixture.Fixtures
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.opintooikeus.{OpintoOikeusRepository, PostgresOpintoOikeusRepository, TorDatabaseFixtures}
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.security.Authentication
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.user.UserRepository
import fi.oph.tor.util.TimedProxy
import fi.vm.sade.security.ldap.DirectoryClient

object TorApplication {
  def apply: TorApplication = apply(Map.empty)

  def apply(overrides: Map[String, String]): TorApplication = {
    val config = overrides.toList.foldLeft(ConfigFactory.load)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
    new TorApplication(config)
  }
}

class TorApplication(val config: Config) {
  lazy val directoryClient: DirectoryClient = Authentication.directoryClient(config)
  lazy val oppijaRepository = OppijaRepository(config)
  lazy val tutkintoRepository = new TutkintoRepository(EPerusteetRepository.apply(config))
  lazy val oppilaitosRepository = new OppilaitosRepository
  lazy val koodistoPalvelu = KoodistoPalvelu(config)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoPalvelu)
  lazy val userRepository = UserRepository(config)
  lazy val database = new TorDatabase(config)
  lazy val opintoOikeusRepository = TimedProxy[OpintoOikeusRepository](new PostgresOpintoOikeusRepository(database.db))


  def resetFixtures = if(Fixtures.shouldUseFixtures(config)) {
    oppijaRepository.resetFixtures
    TorDatabaseFixtures.resetFixtures(database)
  }
}
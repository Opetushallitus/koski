package fi.oph.koski.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.cache.CachingStrategy.cacheAllRefresh
import fi.oph.koski.cache.{CachingProxy, GlobalCacheInvalidator}
import fi.oph.koski.db._
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.{FixtureCreator, Fixtures}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koski.{KoskiFacade, KoskiValidator}
import fi.oph.koski.koskiuser.{CasTicketSessionRepository, UserAuthenticationContext, DirectoryClientFactory, KäyttöoikeusRepository}
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.opiskeluoikeus.{AuxiliaryOpiskeluOikeusRepository, CompositeOpiskeluOikeusRepository, OpiskeluOikeusRepository, PostgresOpiskeluOikeusRepository}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrAccessChecker, YtrOpiskeluoikeusRepository}

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(val config: Config) extends Logging with UserAuthenticationContext {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = CachingProxy(cacheAllRefresh("TutkintoRepository", 3600, 100), TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu))
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = new KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val authenticationServiceClient = AuthenticationServiceClient(config, Some(database.db))
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(authenticationServiceClient, organisaatioRepository)
  lazy val database = new KoskiDatabase(config)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YlioppilasTutkintoRekisteri(config)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val oppijaRepository = OppijaRepository(authenticationServiceClient, koodistoViitePalvelu, virtaClient, virtaAccessChecker, ytrClient, ytrAccessChecker)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(database.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluOikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, oppijaRepository, oppilaitosRepository, koodistoViitePalvelu, virtaAccessChecker, Some(validator)))
  lazy val possu = TimedProxy[OpiskeluOikeusRepository](new PostgresOpiskeluOikeusRepository(database.db, historyRepository))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluOikeusRepository](YtrOpiskeluoikeusRepository(ytrClient, oppijaRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator)))
  lazy val opiskeluOikeusRepository = new CompositeOpiskeluOikeusRepository(possu, List(virta, ytr))
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)
  lazy val facade = new KoskiFacade(oppijaRepository, opiskeluOikeusRepository)
  lazy val serviceTicketRepository = new CasTicketSessionRepository(database.db)
  lazy val fixtureCreator = new FixtureCreator(config, database, opiskeluOikeusRepository, oppijaRepository, validator)

  def invalidateCaches = GlobalCacheInvalidator.invalidateCache
}
package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.cache.Cache.cacheAllRefresh
import fi.oph.koski.cache.{CacheManager, CachingProxy}
import fi.oph.koski.sso.SSOTicketSessionRepository
import fi.oph.koski.db._
import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.healthcheck.HealthCheck
import fi.oph.koski.henkilo.{AuthenticationServiceClient, HenkilöRepository, KoskiHenkilöCache, KoskiHenkilöCacheUpdater}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.tiedonsiirto.{TiedonsiirtoFailureMailer, TiedonsiirtoService}
import fi.oph.koski.tutkinto.TutkintoRepository
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.virta.{VirtaAccessChecker, VirtaClient, VirtaOpiskeluoikeusRepository}
import fi.oph.koski.ytr.{YlioppilasTutkintoRekisteri, YtrAccessChecker, YtrOpiskeluoikeusRepository}

object KoskiApplication {
  lazy val defaultConfig = ConfigFactory.load

  def apply: KoskiApplication = apply(defaultConfig)

  def apply(config: Config): KoskiApplication = new KoskiApplication(config)
}

class KoskiApplication(val config: Config, implicit val cacheManager: CacheManager = new CacheManager) extends Logging with UserAuthenticationContext {
  lazy val organisaatioRepository = OrganisaatioRepository(config, koodistoViitePalvelu)
  lazy val directoryClient = DirectoryClientFactory.directoryClient(config)
  lazy val tutkintoRepository = TutkintoRepository(EPerusteetRepository.apply(config), arviointiAsteikot, koodistoViitePalvelu)
  lazy val oppilaitosRepository = new OppilaitosRepository(organisaatioRepository)
  lazy val koodistoPalvelu = KoodistoPalvelu.apply(config)
  lazy val koodistoViitePalvelu = KoodistoViitePalvelu(koodistoPalvelu)
  lazy val arviointiAsteikot = ArviointiasteikkoRepository(koodistoViitePalvelu)
  lazy val authenticationServiceClient = AuthenticationServiceClient(config, database.db, perustiedotRepository)
  lazy val käyttöoikeusRepository = new KäyttöoikeusRepository(authenticationServiceClient, organisaatioRepository, directoryClient)
  lazy val userRepository = new KoskiUserRepository(authenticationServiceClient)
  lazy val database = new KoskiDatabase(config)
  lazy val virtaClient = VirtaClient(config)
  lazy val ytrClient = YlioppilasTutkintoRekisteri(config)
  lazy val virtaAccessChecker = new VirtaAccessChecker(käyttöoikeusRepository)
  lazy val ytrAccessChecker = new YtrAccessChecker(käyttöoikeusRepository)
  lazy val henkilöRepository = HenkilöRepository(this)
  lazy val historyRepository = OpiskeluoikeusHistoryRepository(database.db)
  lazy val virta = TimedProxy[AuxiliaryOpiskeluoikeusRepository](VirtaOpiskeluoikeusRepository(virtaClient, henkilöRepository, oppilaitosRepository, koodistoViitePalvelu, virtaAccessChecker, Some(validator)))
  lazy val henkilöCache = new KoskiHenkilöCache(perustiedotRepository)
  lazy val henkilöCacheUpdater = new KoskiHenkilöCacheUpdater(database.db, henkilöRepository)
  lazy val possu = TimedProxy[OpiskeluoikeusRepository](new PostgresOpiskeluoikeusRepository(database.db, historyRepository, henkilöCacheUpdater))
  lazy val ytr = TimedProxy[AuxiliaryOpiskeluoikeusRepository](YtrOpiskeluoikeusRepository(ytrClient, henkilöRepository, organisaatioRepository, oppilaitosRepository, koodistoViitePalvelu, ytrAccessChecker, Some(validator)))
  lazy val opiskeluoikeusRepository = new CompositeOpiskeluoikeusRepository(possu, List(virta, ytr))
  lazy val opiskeluoikeusQueryRepository = new PostgresOpiskeluoikeusQueryService(database.db)
  lazy val validator: KoskiValidator = new KoskiValidator(tutkintoRepository, koodistoViitePalvelu, organisaatioRepository)
  val perustiedotRepository = new OpiskeluoikeudenPerustiedotRepository(config, opiskeluoikeusQueryRepository)
  lazy val oppijaFacade = new KoskiOppijaFacade(henkilöRepository, opiskeluoikeusRepository, perustiedotRepository, config)
  lazy val sessionTimeout = SessionTimeout(config)
  lazy val serviceTicketRepository = new SSOTicketSessionRepository(database.db, sessionTimeout)
  lazy val fixtureCreator = new FixtureCreator(config, database, opiskeluoikeusRepository, henkilöRepository, perustiedotRepository, validator)
  lazy val tiedonsiirtoService = new TiedonsiirtoService(database.db, new TiedonsiirtoFailureMailer(config, authenticationServiceClient), organisaatioRepository, henkilöRepository, koodistoViitePalvelu, userRepository)
  lazy val healthCheck = HealthCheck(this)
}
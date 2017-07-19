import javax.servlet.ServletContext

import fi.oph.koski.{IndexServlet, LoginPageServlet}
import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.sso.{CasServlet, LocalLoginServlet, SSOConfig}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db._
import fi.oph.koski.documentation.{DocumentationServlet, DocumentationApiServlet, KoodistoServlet}
import fi.oph.koski.editor.EditorServlet
import fi.oph.koski.fixture.{FixtureServlet, Fixtures}
import fi.oph.koski.healthcheck.{HealthCheckApiServlet, HealthCheckHtmlServlet}
import fi.oph.koski.henkilo.HenkilötiedotServlet
import fi.oph.koski.history.KoskiHistoryServlet
import fi.oph.koski.koodisto.{KoodistoCreator, Koodistot}
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizationServlet
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusServlet, OpiskeluoikeusValidationServlet}
import fi.oph.koski.oppija.OppijaServlet
import fi.oph.koski.oppilaitos.OppilaitosServlet
import fi.oph.koski.organisaatio.OrganisaatioServlet
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotServlet
import fi.oph.koski.preferences.PreferencesServlet
import fi.oph.koski.pulssi.{PulssiHtmlServlet, PulssiServlet}
import fi.oph.koski.suoritusote.SuoritusServlet
import fi.oph.koski.tiedonsiirto.TiedonsiirtoServlet
import fi.oph.koski.todistus.TodistusServlet
import fi.oph.koski.tutkinto.TutkinnonPerusteetServlet
import fi.oph.koski.util.{Futures, Pools}
import fi.oph.koski.validation.KoskiJsonSchemaValidator
import org.scalatra._

import scala.concurrent.Future

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext {
  override def init(context: ServletContext) = tryCatch("Servlet context initialization") {
    def mount(path: String, handler: Handler) = context.mount(handler, path)

    val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiApplication]).getOrElse(KoskiApplication.apply)

    val parallels = List(
      Future { KoskiJsonSchemaValidator.henkilöSchema },
      Future { application.perustiedotIndexer.init},
      Future { application.tiedonsiirtoService.init },
      Future { application.scheduledTasks.toString },
      Future { application.tiedonsiirtoService.toString },
      Future { application.localizationRepository.createMissing() }
    )

    if (application.config.getBoolean("koodisto.create")) tryCatch("Koodistojen luonti") { KoodistoCreator.createKoodistotFromMockData(Koodistot.koskiKoodistot, application.config, application.config.getBoolean("koodisto.update")) }

    mount("/", new IndexServlet(application))
    mount("/login", new LoginPageServlet(application))
    mount("/pulssi", new PulssiHtmlServlet(application))
    mount("/todistus", new TodistusServlet(application))
    mount("/opintosuoritusote", new SuoritusServlet(application))
    mount("/dokumentaatio", new DocumentationServlet(application))
    mount("/api/documentation", new DocumentationApiServlet(application.koodistoPalvelu))
    mount("/api/editor", new EditorServlet(application))
    mount("/api/healthcheck", new HealthCheckApiServlet(application))
    mount("/api/henkilo", new HenkilötiedotServlet(application))
    mount("/api/koodisto", new KoodistoServlet(application.koodistoPalvelu))
    mount("/api/opiskeluoikeus", new OpiskeluoikeusServlet(application))
    mount("/api/opiskeluoikeus/perustiedot", new OpiskeluoikeudenPerustiedotServlet(application))
    mount("/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet(application))
    mount("/api/opiskeluoikeus/historia", new KoskiHistoryServlet(application))
    mount("/api/oppija", new OppijaServlet(application))
    mount("/api/oppilaitos", new OppilaitosServlet(application))
    mount("/api/organisaatio", new OrganisaatioServlet(application))
    mount("/api/pulssi", new PulssiServlet(application))
    mount("/api/preferences", new PreferencesServlet(application))
    mount("/api/tiedonsiirrot", new TiedonsiirtoServlet(application))
    mount("/api/tutkinnonperusteet", new TutkinnonPerusteetServlet(application.tutkintoRepository, application.koodistoViitePalvelu))
    mount("/api/localization", new LocalizationServlet(application))
    mount("/healthcheck", new HealthCheckHtmlServlet(application))
    mount("/user", new UserServlet(application))
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/user/login", new LocalLoginServlet(application))
    }
    mount("/user/logout", new LogoutServlet(application))
    mount("/cas", new CasServlet(application))
    mount("/cache", new CacheServlet(application))

    parallels.foreach(f => Futures.await(f))

    if (Fixtures.shouldUseFixtures(application.config)) {
      context.mount(new FixtureServlet(application), "/fixtures")
      application.fixtureCreator.resetFixtures
    }
  }

  override def destroy(context: ServletContext) = {
  }

  private def tryCatch(thing: String)(task: => Unit): Unit = {
    try {
      task
    } catch {
      case e: Exception => logger.error(e)(thing + " epäonnistui: " + e.getMessage)
    }
  }
}

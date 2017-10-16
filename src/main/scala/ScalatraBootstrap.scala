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
import fi.oph.koski.servlet.RedirectServlet
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

    implicit val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiApplication]).getOrElse(KoskiApplication.apply)

    val parallels = List(
      Future { application.tiedonsiirtoService.init },
      Future { application.scheduledTasks.init },
      Future { application.localizationRepository.createMissing }
    )

    tryCatch("Koodistojen luonti") { KoodistoCreator(application.config).createAndUpdateCodesBasedOnMockData }

    mount("/", new IndexServlet)
    mount("/login", new LoginPageServlet)
    mount("/pulssi", new PulssiHtmlServlet)
    mount("/todistus", new TodistusServlet)
    mount("/opintosuoritusote", new SuoritusServlet)
    mount("/dokumentaatio", new RedirectServlet("/documentation", true))
    mount("/documentation", new DocumentationServlet)
    mount("/api/documentation", new DocumentationApiServlet)
    mount("/api/editor", new EditorServlet)
    mount("/api/healthcheck", new HealthCheckApiServlet)
    mount("/api/henkilo", new HenkilötiedotServlet)
    mount("/api/koodisto", new KoodistoServlet)
    mount("/api/opiskeluoikeus", new OpiskeluoikeusServlet)
    mount("/api/opiskeluoikeus/perustiedot", new OpiskeluoikeudenPerustiedotServlet)
    mount("/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet)
    mount("/api/opiskeluoikeus/historia", new KoskiHistoryServlet)
    mount("/api/oppija", new OppijaServlet)
    mount("/api/oppilaitos", new OppilaitosServlet)
    mount("/api/organisaatio", new OrganisaatioServlet)
    mount("/api/pulssi", new PulssiServlet)
    mount("/api/preferences", new PreferencesServlet)
    mount("/api/tiedonsiirrot", new TiedonsiirtoServlet)
    mount("/api/tutkinnonperusteet", new TutkinnonPerusteetServlet)
    mount("/api/localization", new LocalizationServlet)
    mount("/healthcheck", new HealthCheckHtmlServlet)
    mount("/user", new UserServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/user/login", new LocalLoginServlet)
    }
    mount("/user/logout", new LogoutServlet)
    mount("/cas", new CasServlet)
    mount("/cache", new CacheServlet)

    parallels.foreach(f => Futures.await(f))

    if (Fixtures.shouldUseFixtures(application.config)) {
      context.mount(new FixtureServlet, "/fixtures")
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

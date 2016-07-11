import javax.servlet.ServletContext

import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db._
import fi.oph.koski.documentation.SchemaDocumentationServlet
import fi.oph.koski.fixture.{FixtureServlet, Fixtures}
import fi.oph.koski.history.KoskiHistoryServlet
import fi.oph.koski.koodisto.KoodistoCreator
import fi.oph.koski.koski.{KoskiFacade, OppijaServlet}
import fi.oph.koski.koskiuser.{KäyttöoikeusRyhmätCreator, LogoutServlet, KäyttöoikeusRepository, UserServlet}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosServlet
import fi.oph.koski.servlet.IndexServlet
import fi.oph.koski.suoritusote.SuoritusServlet
import fi.oph.koski.todistus.TodistusServlet
import fi.oph.koski.tutkinto.TutkintoServlet
import fi.oph.koski.util.Pools
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    try {
      Pools.init
      val configOverrides: Map[String, String] = Option(context.getAttribute("tor.overrides").asInstanceOf[Map[String, String]]).getOrElse(Map.empty)
      val application = KoskiApplication(configOverrides)
      if (application.config.getBoolean("koodisto.create")) {
        KoodistoCreator.createKoodistotFromMockData(application.config)
      }
      if (application.config.getBoolean("käyttöoikeusryhmät.create")) {
        KäyttöoikeusRyhmätCreator.luoKäyttöoikeusRyhmät(application.config)
      }

      context.mount(new OppijaServlet(application), "/api/oppija")
      context.mount(new KoskiHistoryServlet(application), "/api/opiskeluoikeus/historia")
      context.mount(new UserServlet(application), "/user")
      context.mount(new LogoutServlet(application), "/user/logout")
      context.mount(new OppilaitosServlet(application), "/api/oppilaitos")
      context.mount(new TutkintoServlet(application.tutkintoRepository), "/api/tutkinto")
      context.mount(new SchemaDocumentationServlet(application.koodistoPalvelu), "/documentation")
      context.mount(new TodistusServlet(application), "/todistus")
      context.mount(new SuoritusServlet(application), "/opintosuoritusote")
      context.mount(new IndexServlet(application), "/")
      context.mount(new CacheServlet(application), "/cache")
      if (Fixtures.shouldUseFixtures(application.config)) {
        context.mount(new FixtureServlet(application), "/fixtures")
      }
    } catch {
      case e: Throwable =>
        logger.error(e)("Error in server startup")
        System.exit(1)
    }
  }

  override def destroy(context: ServletContext) = {
  }
}
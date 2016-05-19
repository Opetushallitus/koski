import javax.servlet.ServletContext
import fi.oph.tor.cache.CacheServlet
import fi.oph.tor.config.TorApplication
import fi.oph.tor.db._
import fi.oph.tor.documentation.SchemaDocumentationServlet
import fi.oph.tor.fixture.{FixtureServlet, Fixtures}
import fi.oph.tor.history.TorHistoryServlet
import fi.oph.tor.koodisto.KoodistoCreator
import fi.oph.tor.oppilaitos.OppilaitosServlet
import fi.oph.tor.servlet.{StaticFileServlet, SingleFileServlet}
import fi.oph.tor.suoritusote.SuoritusServlet
import fi.oph.tor.todistus.TodistusServlet
import fi.oph.tor.tor.{TodennetunOsaamisenRekisteri, OppijaServlet, TorValidator}
import fi.oph.tor.toruser.{AuthenticationServlet, UserOrganisationsRepository}
import fi.oph.tor.tutkinto.TutkintoServlet
import fi.oph.tor.log.Logging
import fi.oph.tor.util.Pools
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    try {
      Pools.init
      val configOverrides: Map[String, String] = Option(context.getAttribute("tor.overrides").asInstanceOf[Map[String, String]]).getOrElse(Map.empty)
      val application = TorApplication(configOverrides)
      if (application.config.getBoolean("koodisto.create")) {
        KoodistoCreator.createKoodistotFromMockData(application.config)
      }
      implicit val userRepository = UserOrganisationsRepository(application.config, application.organisaatioRepository)

      val rekisteri = new TodennetunOsaamisenRekisteri(application.oppijaRepository, application.opiskeluOikeusRepository)
      context.mount(new OppijaServlet(rekisteri, userRepository, application.directoryClient, application.validator, application.historyRepository), "/api/oppija")
      context.mount(new TorHistoryServlet(userRepository, application.directoryClient, application.historyRepository), "/api/opiskeluoikeus/historia")
      context.mount(new AuthenticationServlet(application.directoryClient, application.userRepository), "/user")
      context.mount(new OppilaitosServlet(application.oppilaitosRepository, application.userRepository, application.directoryClient), "/api/oppilaitos")
      context.mount(new TutkintoServlet(application.tutkintoRepository), "/api/tutkinto")
      context.mount(new SchemaDocumentationServlet(application.koodistoPalvelu), "/documentation")
      context.mount(new TodistusServlet(userRepository, application.directoryClient, rekisteri, application.tutkintoRepository), "/todistus")
      context.mount(new SuoritusServlet(userRepository, application.directoryClient, rekisteri, application.oppijaRepository, application.opiskeluOikeusRepository), "/opintosuoritusote")
      val indexHtml = StaticFileServlet.contentOf("web/static/index.html").get
      context.mount(new SingleFileServlet(indexHtml, List(("/*", 404), ("/uusioppija", 200), ("/oppija/:oid", 200))), "/")
      context.mount(new CacheServlet(userRepository, application.directoryClient, application), "/cache")
      if (Fixtures.shouldUseFixtures(application.config)) {
        context.mount(new FixtureServlet(application), "/fixtures")
      }
    } catch {
      case e: Throwable =>
        logger.error("Error in server startup", e)
        System.exit(1)
    }
  }

  override def destroy(context: ServletContext) = {
  }
}
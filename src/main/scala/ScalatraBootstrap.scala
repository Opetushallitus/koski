import javax.servlet.ServletContext

import fi.oph.tor.SingleFileServlet
import fi.oph.tor.config.TorApplication
import fi.oph.tor.db._
import fi.oph.tor.fixture.{FixtureServlet, Fixtures}
import fi.oph.tor.history.TorHistoryServlet
import fi.oph.tor.koodisto.KoodistoCreator
import fi.oph.tor.oppilaitos.OppilaitosServlet
import fi.oph.tor.schema.SchemaDocumentationServlet
import fi.oph.tor.tor.{TodennetunOsaamisenRekisteri, TorServlet, TorValidator}
import fi.oph.tor.toruser.{AuthenticationServlet, UserOrganisationsRepository}
import fi.oph.tor.tutkinto.TutkintoServlet
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    val configOverrides: Map[String, String] = Option(context.getAttribute("tor.overrides").asInstanceOf[Map[String, String]]).getOrElse(Map.empty)
    val application = TorApplication(configOverrides)
    if (application.config.getBoolean("koodisto.create")) {
      KoodistoCreator.createKoodistotFromMockData(application.config)
    }
    implicit val userRepository = UserOrganisationsRepository(application.config, application.organisaatioRepository)
    val validator: TorValidator = new TorValidator(application.tutkintoRepository, application.koodistoPalvelu, application.organisaatioRepository)
    val rekisteri = new TodennetunOsaamisenRekisteri(application.oppijaRepository, application.opiskeluOikeusRepository)
    context.mount(new TorServlet(rekisteri, userRepository, application.directoryClient, validator, application.historyRepository), "/api/oppija")
    context.mount(new TorHistoryServlet(userRepository, application.directoryClient, application.historyRepository), "/api/opiskeluoikeus/historia")
    context.mount(new AuthenticationServlet(application.directoryClient), "/user")
    context.mount(new OppilaitosServlet(application.oppilaitosRepository, application.userRepository, application.directoryClient), "/api/oppilaitos")
    context.mount(new TutkintoServlet(application.tutkintoRepository), "/api/tutkinto")
    context.mount(new SingleFileServlet("web/static/index.html"), "/")
    context.mount(new SchemaDocumentationServlet(application.lowLevelKoodistoPalvelu), "/documentation")

    if (Fixtures.shouldUseFixtures(application.config)) {
      context.mount(new FixtureServlet(application), "/fixtures")
    }
  }

  override def destroy(context: ServletContext) = {
  }
}
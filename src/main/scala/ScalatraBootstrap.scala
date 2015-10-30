import javax.servlet.ServletContext

import fi.oph.tor.SingleFileServlet
import fi.oph.tor.config.TorProfile
import fi.oph.tor.db._
import fi.oph.tor.fixture.FixtureServlet
import fi.oph.tor.oppija.OppijaServlet
import fi.oph.tor.oppilaitos.OppilaitosServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.tutkinto.TutkintoServlet
import fi.oph.tor.user.{UserRepository, UserServlet}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    val profile: TorProfile with GlobalExecutionContext = Option(context.getAttribute("tor.profile").asInstanceOf[String]).map(TorProfile.fromString(_)).getOrElse(TorProfile.fromSystemProperty)
    val database: TorDatabase = profile.database
    implicit val userRepository = UserRepository(profile.config)
    val rekisteri = new TodennetunOsaamisenRekisteri(profile.oppijaRepository, profile.opintoOikeusRepository, profile.tutkintoRepository, profile.oppilaitosRepository, profile.arviointiAsteikot)
    context.mount(new OppijaServlet(rekisteri), "/api/oppija")
    context.mount(new UserServlet(profile.directoryClient, profile.userRepository), "/user")
    context.mount(new SingleFileServlet("web/static/index.html"), "/oppija")
    context.mount(new SingleFileServlet("web/static/index.html"), "/uusioppija")
    context.mount(new OppilaitosServlet(profile.oppilaitosRepository), "/api/oppilaitos")
    context.mount(new TutkintoServlet(profile.tutkintoRepository, profile.arviointiAsteikot), "/api/tutkinto")
    context.mount(new FixtureServlet(profile), "/fixtures")
    context.mount(new SingleFileServlet("web/static/index.html"), "/")
  }

  override def destroy(context: ServletContext) = {
  }
}
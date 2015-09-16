import javax.servlet.ServletContext

import fi.oph.tor.config.TorProfile
import fi.oph.tor.db._
import fi.oph.tor.oppija.OppijaServlet
import fi.oph.tor.user.{LogoutServlet, LoginServlet, UserServlet}
import fi.oph.tor.{SuoritusServlet, TodennetunOsaamisenRekisteri}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    val profile = Option(context.getAttribute("tor.profile").asInstanceOf[String]).map(TorProfile.fromString(_)).getOrElse(TorProfile.fromSystemProperty)
    val database: TorDatabase = profile.database
    val rekisteri = new TodennetunOsaamisenRekisteri(database.db)

    context.mount(new SuoritusServlet(rekisteri), "/suoritus")
    context.mount(new OppijaServlet(profile.oppijaRepository), "/oppija")
    context.mount(new UserServlet, "/user")
    context.mount(new LoginServlet(profile.directoryClient), "/login")
    context.mount(new LogoutServlet, "/logout")

    //await(database.run(DatabaseTestFixture.clear))
    //TestFixture.apply(rekisteri)
  }

  override def destroy(context: ServletContext) = {
  }
}
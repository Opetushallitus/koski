import javax.servlet.ServletContext

import fi.oph.tor.{SuoritusServlet, TodennetunOsaamisenRekisteri, HelloWorldServlet}
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.fixture.TestFixture
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  val database: TorDatabase = TorDatabase.init(DatabaseConfig.localDatabase)
  val rekisteri = new TodennetunOsaamisenRekisteri(database.db)

  override def init(context: ServletContext) {
    context.mount(new HelloWorldServlet, "/")
    context.mount(new SuoritusServlet(rekisteri), "/suoritus")

    //await(database.run(DatabaseTestFixture.clear))
    //TestFixture.apply(rekisteri)
  }

  override def destroy(context: ServletContext) = {
  }
}
import javax.servlet.ServletContext

import fi.oph.tor.{TutkintosuoritusServlet, TodennetunOsaamisenRekisteri, HelloWorldServlet}
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db._
import fi.oph.tor.fixture.TestFixture
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  val database: DB = TorDatabase.forConfig(DatabaseConfig.localPostgresDatabase)
  val rekisteri = new TodennetunOsaamisenRekisteri(database)

  override def init(context: ServletContext) {
    context.mount(new HelloWorldServlet, "/")
    context.mount(new TutkintosuoritusServlet(rekisteri), "/tutkintosuoritus")

    await(database.run(DatabaseTestFixture.clear))
    TestFixture.apply(rekisteri)
  }

  override def destroy(context: ServletContext) = {
  }
}
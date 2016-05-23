package fi.oph.tor.fixture

import fi.oph.tor.config.TorApplication
import fi.oph.tor.servlet.{ApiServlet, KoskiBaseServlet}

// TODO: require superuser privileged
class FixtureServlet(application: TorApplication) extends ApiServlet {
  post("/reset") {
    application.resetFixtures
  }
}

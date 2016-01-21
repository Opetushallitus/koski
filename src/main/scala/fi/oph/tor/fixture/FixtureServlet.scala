package fi.oph.tor.fixture

import fi.oph.tor.config.TorApplication
import fi.oph.tor.servlet.ErrorHandlingServlet

class FixtureServlet(application: TorApplication) extends ErrorHandlingServlet {
  post("/reset") {
    application.resetFixtures
  }
}

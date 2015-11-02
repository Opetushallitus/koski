package fi.oph.tor.fixture

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.config.TorApplication

class FixtureServlet(application: TorApplication) extends ErrorHandlingServlet {
  post("/reset") {
    application.resetFixtures
  }
}

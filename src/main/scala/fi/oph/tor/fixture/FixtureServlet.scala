package fi.oph.tor.fixture

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.config.TorProfile

class FixtureServlet(profile: TorProfile) extends ErrorHandlingServlet {
  post("/reset") {
    profile.resetMocks
  }
}

package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.ApiServlet

// TODO: require superuser privileged
class FixtureServlet(application: KoskiApplication) extends ApiServlet {
  post("/reset") {
    application.resetFixtures
  }
}

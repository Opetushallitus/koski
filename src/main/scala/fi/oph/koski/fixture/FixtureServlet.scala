package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.ApiServletWithSchemaBasedSerialization

class FixtureServlet(implicit val application: KoskiApplication) extends ApiServletWithSchemaBasedSerialization with RequiresAuthentication {
  post("/reset") {
    application.fixtureCreator.resetFixtures
    application.elasticSearch.refreshIndex
    "ok"
  }

  post("/refresh") {
    application.elasticSearch.refreshIndex
    "ok"
  }
}

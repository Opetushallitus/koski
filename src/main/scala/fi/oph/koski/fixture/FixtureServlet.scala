package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class FixtureServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/reset") {
    application.fixtureCreator.resetFixtures
    application.elasticSearch.refreshIndex
    "ok"
  }

  post("/refresh") {
    application.elasticSearch.refreshIndex
    "ok"
  }

  post("/sync-tiedonsiirrot") {
    application.tiedonsiirtoService.syncToElasticsearch(refreshIndex = true)
    "ok"
  }

  post("/sync-perustiedot") {
    application.perustiedotSyncScheduler.sync
    application.elasticSearch.refreshIndex
    "ok"
  }
}

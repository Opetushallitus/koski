package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class FixtureServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/reset") {
    application.fixtureCreator.resetFixtures
    application.indexManager.refreshAll()
    "ok"
  }

  post("/refresh") {
    application.indexManager.refreshAll()
    "ok"
  }

  post("/sync-tiedonsiirrot") {
    application.tiedonsiirtoService.syncToElasticsearch(shouldRefreshIndex = true)
    "ok"
  }

  post("/sync-perustiedot") {
    application.perustiedotSyncScheduler.sync
    application.perustiedotIndexer.refreshIndex
    "ok"
  }
}

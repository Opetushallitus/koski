package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class FixtureServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/reset") {
    application.fixtureCreator.resetFixtures(reloadRaportointikanta = true)
    "ok"
  }

  post("/sync-tiedonsiirrot") {
    application.tiedonsiirtoService.syncToElasticsearch(refresh = true)
    "ok"
  }

  post("/sync-perustiedot") {
    application.perustiedotIndexer.sync(refresh = true)
    "ok"
  }
}

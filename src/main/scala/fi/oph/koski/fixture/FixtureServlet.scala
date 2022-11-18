package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class FixtureServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/reset") {
    val reloadRaportointikanta = params("reloadRaportointikanta") match {
      case "true" | "1" => true
      case "false" | "0" => false
      // Oletuksena true, kuten aiemmin on testeissä ollut
      case _ => true
    }
    application.fixtureCreator.resetFixtures(reloadRaportointikanta = reloadRaportointikanta)
    "ok"
  }

  post("/sync-tiedonsiirrot") {
    application.tiedonsiirtoService.syncToOpenSearch(refresh = true)
    "ok"
  }

  post("/sync-perustiedot") {
    application.perustiedotIndexer.sync(refresh = true)
    "ok"
  }
}

package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureState, FixtureUtil, StatefulFixtureUtil}
import fi.oph.koski.valpas.servlet.ValpasApiServlet

class ValpasTestApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with Unauthenticated {
  private val fixtureReset = new StatefulFixtureUtil(application)

  before() {
    // Tämä on ylimääräinen varmistus: tämän servletin ei koskaan pitäisi päätyä ajoon kuin mock-moodissa
    application.config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
      case _ => {
        val status = KoskiErrorCategory.internalError
        halt(status.statusCode, status)
      }
    }
  }

  get("/reset-mock-data/:paiva") {
    val tarkastelupäivä = getLocalDateParam("paiva")
    fixtureReset.resetMockData(tarkastelupäivä)
  }

  get("/reset-mock-data") {
    fixtureReset.resetMockData()
  }

  get("/clear-mock-data") {
    fixtureReset.clearMockData()
  }

  get("/current-mock-status") {
    FixtureState(application)
  }

  get("/logout/:user") {
    application.koskiSessionRepository.removeSessionByUsername(getStringParam("user"))
  }
}


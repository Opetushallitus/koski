package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.fixture.{FixtureState, FixtureUtil}
import fi.oph.koski.valpas.repository.MockRajapäivät
import fi.oph.koski.valpas.servlet.ValpasApiServlet

class ValpasTestApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with Unauthenticated {
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
    val tarkasteluPäivä = getLocalDateParam("paiva")
    FixtureUtil.resetMockData(application, new MockRajapäivät(tarkasteluPäivä))
  }

  get("/reset-mock-data") {
    FixtureUtil.resetMockData(application)
  }

  get("/clear-mock-data") {
    FixtureUtil.clearMockData(application)
  }

  get("/current-mock-status") {
    FixtureState(application)
  }
}


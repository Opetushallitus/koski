package fi.oph.koski.valpas

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.repository.{MockRajapäivät, OikeatRajapäivät}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

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
    val tarkasteluPäivä = LocalDate.parse(getStringParam("paiva"))
    resetMockData(MockRajapäivät(tarkasteluPäivä))
  }

  get("/reset-mock-data") {
    resetMockData(MockRajapäivät())
  }

  get("/clear-mock-data") {
    synchronized {
      ValpasMockUsers.mockUsersEnabled = false
      application.fixtureCreator.resetFixtures(application.fixtureCreator.koskiSpecificFixtureState)
      contentType = "text/json"
      response.writer.print("\"Valpas mock data cleared\"")
      MockRajapäivät.mockRajapäivät = OikeatRajapäivät()
    }
  }

  private def resetMockData(rajapäivät: MockRajapäivät) = {
    synchronized {
      ValpasMockUsers.mockUsersEnabled = true
      application.fixtureCreator.resetFixtures(application.fixtureCreator.valpasFixtureState)
      contentType = "text/json"
      response.writer.print("\"Valpas mock data reset\"")
      val tarkasteluPäivä = LocalDate.parse(getStringParam("paiva"))
      MockRajapäivät.mockRajapäivät = rajapäivät
    }
  }
}

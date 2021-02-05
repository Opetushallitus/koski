package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasTestApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {
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

  get("/reset-mock-data") {
    ValpasMockUsers.mockUsersEnabled = true
    application.directoryClient.invalidateCache()
    contentType = "text/json"
    response.writer.print("\"Valpas mock data reset\"")
  }

  get("/clear-mock-data") {
    ValpasMockUsers.mockUsersEnabled = false
    application.directoryClient.invalidateCache()
    contentType = "text/json"
    response.writer.print("\"Valpas mock data cleared\"")
  }
}

package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSession, Unauthenticated}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class ValpasLocalizationServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {

  get("/set-localization-map.js") {
    contentType = "text/javascript"
    response.writer.print("window.valpasLocalizationMap=" + JsonSerializer.writeWithRoot(application.valpasLocalizationRepository.localizations))
  }
}

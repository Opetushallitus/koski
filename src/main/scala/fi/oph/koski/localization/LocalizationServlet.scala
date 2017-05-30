package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{RequiresAuthentication, Unauthenticated}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class LocalizationServlet (val application: KoskiApplication) extends ApiServlet with Unauthenticated with NoCache {
  get("/") {
    application.localizationRepository.localizations()
  }
}

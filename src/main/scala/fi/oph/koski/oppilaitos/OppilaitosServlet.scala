package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiSession).toList
  }
}
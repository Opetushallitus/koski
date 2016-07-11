package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.ApiServlet

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiUser).toList
  }
}

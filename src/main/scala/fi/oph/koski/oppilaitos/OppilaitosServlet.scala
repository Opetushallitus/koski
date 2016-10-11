package fi.oph.koski.oppilaitos

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.{ApiServlet, Cached24Hours, UserSessionCached}

class OppilaitosServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Cached24Hours with UserSessionCached {
  get("/") {
    application.oppilaitosRepository.oppilaitokset(koskiUser).toList
  }
}
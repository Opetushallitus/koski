package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.{ApiServlet, NoCache}

class UserServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/") {
    renderEither(getUser)
  }
}
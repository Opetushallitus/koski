package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.CurrentUser

class UserServlet extends ErrorHandlingServlet with CurrentUser {
  get("/") {
    getAuthenticatedUser match {
      case Some(user) => Json.write(user)
      case None => halt(401)
    }
  }
}
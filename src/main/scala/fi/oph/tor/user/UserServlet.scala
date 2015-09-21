package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json

class UserServlet extends ErrorHandlingServlet {
  get("/") {
    Option(request.getSession(false)).flatMap(session => Option(session.getAttribute("tor-user"))) match {
      case Some(user) => Json.write(user)
      case None => halt(401)
    }
  }
}
package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.AuthenticationSupport
import fi.vm.sade.security.ldap.DirectoryClient


class UserServlet(val directoryClient: DirectoryClient, userRepository: UserRepository) extends ErrorHandlingServlet with AuthenticationSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user) => Json.write(user)
      case None => halt(401)
    }
  }

  post("/login") {
    scentry.authenticate()
    contentType = "application/json;charset=utf-8"
    Json.write(userOption)
  }

  get("/logout") {
    Option(request.getSession(false)).foreach(_.invalidate())
    response.redirect("/tor")
  }
}
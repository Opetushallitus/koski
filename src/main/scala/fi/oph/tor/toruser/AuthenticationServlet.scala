package fi.oph.tor.toruser

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.vm.sade.security.ldap.DirectoryClient

class AuthenticationServlet(val directoryClient: DirectoryClient) extends ErrorHandlingServlet with AuthenticationSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user: AuthenticationUser) => Json.write(user)
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
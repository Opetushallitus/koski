package fi.oph.tor.toruser

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.log.{TorOperation, AuditLogMessage, AuditLog}
import fi.oph.tor.servlet.ErrorHandlingServlet
import fi.vm.sade.security.ldap.DirectoryClient

class AuthenticationServlet(val directoryClient: DirectoryClient, val userRepository: UserOrganisationsRepository) extends ErrorHandlingServlet with AuthenticationSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user: AuthenticationUser) => Json.write(user)
      case None => renderStatus(TorErrorCategory.unauthorized())
    }
  }

  post("/login") {
    scentry.authenticate() // Halts on login failure, so the code below won't be run
    AuditLog.log(AuditLogMessage(TorOperation.LOGIN, torUserOption.get, Map()))
    contentType = "application/json;charset=utf-8"
    Json.write(userOption.get)
  }

  get("/logout") {
    Option(request.getSession(false)).foreach(_.invalidate())
    redirectToLogin
  }
}
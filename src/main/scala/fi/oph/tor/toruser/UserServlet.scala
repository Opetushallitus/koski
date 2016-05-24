package fi.oph.tor.toruser

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.log.{AuditLog, AuditLogMessage, TorOperation}
import fi.oph.tor.servlet.ApiServlet
import fi.vm.sade.security.ldap.DirectoryClient

class UserServlet(val directoryClient: DirectoryClient, val userRepository: UserOrganisationsRepository) extends ApiServlet with AuthenticationSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user: AuthenticationUser) =>user
      case None => TorErrorCategory.unauthorized()
    }
  }

  post("/login") {
    scentry.authenticate() // Halts on login failure, so the code below won't be run
    AuditLog.log(AuditLogMessage(TorOperation.LOGIN, torUserOption.get, Map()))
    userOption.get
  }
}
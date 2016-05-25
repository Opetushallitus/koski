package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiOperation}
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.security.ldap.DirectoryClient

class UserServlet(val directoryClient: DirectoryClient, val userRepository: UserOrganisationsRepository) extends ApiServlet with AuthenticationSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user: AuthenticationUser) =>user
      case None => KoskiErrorCategory.unauthorized()
    }
  }

  post("/login") {
    scentry.authenticate() // Halts on login failure, so the code below won't be run
    AuditLog.log(AuditLogMessage(KoskiOperation.LOGIN, koskiUserOption.get, Map()))
    userOption.get
  }
}
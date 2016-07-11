package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiOperation}
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.utils.cas.CasLogout

class UserServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport {
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

  get("/cas") { // Return url for cas login
    redirect("/")
  }

  post("/cas") { // Return url for cas logout
    val logoutRequest = params.get("logoutRequest") match {
      case Some(logoutRequest) =>
        val parsedTicket = CasLogout.parseTicketFromLogoutRequest(logoutRequest)
        logger.info("Got CAS logout for ticket " + parsedTicket)
      case None =>
        logger.warn("Got CAS logout POST without logoutRequest parameter")
    }
  }
}
package fi.oph.koski.koskiuser

import java.util.UUID

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiOperation}
import fi.oph.koski.servlet.{ApiServlet, CasSingleSignOnSupport, JsonBodySnatcher, NoCache}

import scala.util.Try

class UserServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with CasSingleSignOnSupport with NoCache {
  get("/") {
    renderEither(getUser)
  }

  if (!isCasSsoUsed) {
    post("/login") {
      def loginRequestInBody = JsonBodySnatcher.getJsonBody(request).right.toOption flatMap { json =>
        Try(Json.fromJValue[Login](json)).toOption
      }

      loginRequestInBody match {
        case Some(Login(username, password)) =>
          renderEither(tryLogin(username, password).right.map { user =>
            val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
            application.serviceTicketRepository.store(fakeServiceTicket, user)
            logger.info("Fake ticket created: " + fakeServiceTicket)
            val finalUser = user.copy(serviceTicket = Some(fakeServiceTicket))
            setUser(finalUser)
            AuditLog.log(AuditLogMessage(KoskiOperation.LOGIN, koskiUserOption.get, Map()))
            finalUser
          })
        case None =>
          haltWithStatus(KoskiErrorCategory.badRequest("Login request missing from body"))
      }
    }
  }
}
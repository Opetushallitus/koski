package fi.oph.koski.koskiuser

import java.util.UUID

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiOperation}
import fi.oph.koski.servlet.{ApiServlet, CasSingleSignOnSupport, JsonBodySnatcher}

import scala.util.Try

class UserServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with CasSingleSignOnSupport {
  get("/") {
    contentType = "application/json;charset=utf-8"
    userOption match {
      case Some(user: AuthenticationUser) =>user
      case None => KoskiErrorCategory.unauthorized()
    }
  }

  if (!isCasSsoUsed) {
    post("/login") {
      val authUser = {
        val loginRequestInBody = JsonBodySnatcher.getJsonBody(request).right.toOption flatMap { json =>
          Try(Json.fromJValue[Login](json)).toOption
        }

        loginRequestInBody flatMap {
          case Login(username, password) => tryLogin(username, password).map { user =>
            val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
            application.serviceTicketRepository.store(fakeServiceTicket, user)
            logger.info("Fake ticket created: " + fakeServiceTicket)
            user.copy(serviceTicket = Some(fakeServiceTicket))
          }
        }
      }

      authUser match {
        case Some(user) =>
          setUser(user)
          AuditLog.log(AuditLogMessage(KoskiOperation.LOGIN, koskiUserOption.get, Map()))
          user
        case None =>
          userNotAuthenticatedError
      }
    }
  }
}
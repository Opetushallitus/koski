package fi.oph.koski.sso

import java.util.UUID

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiUserLanguage, Login, UserAuthenticationContext}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.{ApiServlet, JsonBodySnatcher, NoCache}

import scala.util.Try

class LocalLoginServlet(val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with SSOSupport with NoCache {
  post("/") {
    def loginRequestInBody = JsonBodySnatcher.getJsonBody(request).right.toOption flatMap { json =>
      Try(Json.fromJValue[Login](json)).toOption
    }

    loginRequestInBody match {
      case Some(Login(username, password)) =>
        renderEither(tryLogin(username, password).right.map { user =>
          val fakeServiceTicket: String = "koski-" + UUID.randomUUID()
          application.koskiSessionRepository.store(fakeServiceTicket, user, LogUserContext.clientIpFromRequest(request))
          logger.info("Local session ticket created: " + fakeServiceTicket)
          val finalUser = user.copy(serviceTicket = Some(fakeServiceTicket))
          setUser(Right(finalUser))
          KoskiUserLanguage.setLanguageCookie(KoskiUserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
          finalUser
        })
      case None =>
        haltWithStatus(KoskiErrorCategory.badRequest("Login request missing from body"))
    }
  }
}
package fi.oph.koski.sso

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.common.koskiuser._
import fi.oph.koski.servlet.{ApiServlet, JsonBodySnatcher, NoCache}

import scala.util.Try

class LocalLoginServlet(implicit val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with SSOSupport with NoCache {
  post("/") {
    def loginRequestInBody = JsonBodySnatcher.getJsonBody(request).right.toOption flatMap { json =>
      Try(JsonSerializer.extract[Login](json)).toOption
    }

    loginRequestInBody match {
      case Some(Login(username, password)) =>
        renderEither[AuthenticationUser](tryLogin(username, password).right.flatMap(user => setUser(Right(localLogin(user)))))
      case None =>
        haltWithStatus(KoskiErrorCategory.badRequest("Login request missing from body"))
    }
  }
}

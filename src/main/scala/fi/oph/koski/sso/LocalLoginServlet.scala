package fi.oph.koski.sso

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser._
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, JsonBodySnatcher, NoCache}

import scala.util.Try

class LocalLoginServlet(implicit val application: UserAuthenticationContext) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with KoskiSpecificSSOSupport with NoCache {
  post("/") {
    def loginRequestInBody = JsonBodySnatcher.getJsonBody(request).right.toOption flatMap { json =>
      Try(JsonSerializer.extract[Login](json)).toOption
    }

    loginRequestInBody match {
      case Some(Login(username, password)) =>
        renderEither[AuthenticationUser](tryLogin(
          username,
          // Mahdollistaa testeille sotkun lisäämiseen salasanaan, jotta vältetään
          // testejä blokkaava "salasanasi löytyi vuodettujen salasanojen listalta" -varoitus.
          password.replaceAll("__.*", "")
        ).right.flatMap(user => setUser(Right(localLogin(user)))))
      case None =>
        haltWithStatus(KoskiErrorCategory.badRequest("Login request missing from body"))
    }
  }
}

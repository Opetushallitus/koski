package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.TorSessionCookie

class UserServlet extends ErrorHandlingServlet {
  get("/") {
    val kookie = Option(request.getCookies).getOrElse(Array.empty).filter((cookie) => cookie.getName == "tor-auth" && cookie.getValue.length > 0).headOption
    kookie match {
      case Some(cookie) =>
        Json.write(TorSessionCookie.userFromCookie(cookie.getValue))
      case _ =>
        halt(401)
    }
  }
}
package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import org.scalatra.Cookie

class LogoutServlet extends ErrorHandlingServlet {
  get("/") {
    response.addCookie(Cookie("tor-auth", ""))
    response.redirect("/")
  }
}

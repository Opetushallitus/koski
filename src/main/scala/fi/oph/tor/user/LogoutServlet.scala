package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import org.scalatra.Cookie

class LogoutServlet extends ErrorHandlingServlet {
  get("/") {
    cookies.delete("tor-auth")
    response.redirect("/tor")
  }
}

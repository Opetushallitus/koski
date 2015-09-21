package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import org.scalatra.Cookie

class LogoutServlet extends ErrorHandlingServlet {
  get("/") {
    Option(request.getSession(false)).foreach(_.invalidate())
    response.redirect("/tor")
  }
}

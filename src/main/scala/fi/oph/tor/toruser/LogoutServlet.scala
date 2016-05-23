package fi.oph.tor.toruser

import fi.oph.tor.servlet.HtmlServlet

class LogoutServlet extends HtmlServlet {
  get("/") {
    Option(request.getSession(false)).foreach(_.invalidate())
    redirectToLogin
  }
}

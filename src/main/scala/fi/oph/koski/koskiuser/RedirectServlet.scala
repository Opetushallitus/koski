package fi.oph.koski.koskiuser

import org.scalatra.ScalatraServlet

class LogoutRedirectServlet extends ScalatraServlet {
  /**
    * Simple servlet for redirecting to whatever URL is passed as a parameter.
    * Required for Shibboleth logout, as it will only redirect to whitelisted addresses.
    */

  get("/") {
    redirect(params("target"))
  }
}

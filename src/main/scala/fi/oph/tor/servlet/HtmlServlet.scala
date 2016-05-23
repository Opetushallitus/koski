package fi.oph.tor.servlet

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import org.scalatra.ScalatraServlet

trait HtmlServlet extends ScalatraServlet {
  def redirectToLogin = {
    redirect("/")
  }

  def renderStatus(status: HttpStatus) = {
    status.statusCode match {
      case 401 =>
        redirectToLogin
      case _ =>
        // TODO: 404 page, 500 page instead of JSON
        halt(status = status.statusCode, body = Json.write(status.errors))
    }
  }
}

package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import org.scalatra.ScalatraServlet

class UserServlet extends ErrorHandlingServlet {
  get("/") {
    Json.write(User("12345", "Kalle Käyttäjä"))
  }
}

case class User(oid: String, name: String)
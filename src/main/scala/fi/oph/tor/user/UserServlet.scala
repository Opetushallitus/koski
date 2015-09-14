package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json

class UserServlet extends ErrorHandlingServlet {
  get("/") {
    if (request.getCookies.filter(_.getName == "tor-auth").length > 0) {
      Json.write(User("12345", "Kalle Käyttäjä"))
    } else {
      halt(401)
    }
  }
}
package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json

class UserServlet extends ErrorHandlingServlet {
  get("/") {
    if (request.getCookies != null && request.getCookies.filter((cookie) => cookie.getName == "tor-auth" && cookie.getValue.length > 0).length > 0) {
      Json.write(User("12345", "Kalle Käyttäjä"))
    } else {
      halt(401)
    }
  }
}
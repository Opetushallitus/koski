package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import org.scalatra.Cookie

class LoginServlet extends ErrorHandlingServlet {
  post("/") {
    val login = Json.read[Login](request.body)
    response.addCookie(Cookie("tor-auth", "balaillaan!"))
    Json.write(User("12345", login.username))
  }
}
package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import org.scalatra.Cookie

class LoginServlet extends ErrorHandlingServlet {
  post("/") {
    Thread.sleep(100)
    val login = Json.read[Login](request.body)
    if(login.username == "fail") {
      halt(401, reason = "Invalid password or username")
    }
    response.addCookie(Cookie("tor-auth", "balaillaan!"))
    Json.write(User("12345", login.username))
  }
}
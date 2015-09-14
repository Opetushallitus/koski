package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json

class LoginServlet extends ErrorHandlingServlet {
  post("/") {
    val login = Json.read[Login](request.body)
    Json.write(User("12345", login.username))
  }
}
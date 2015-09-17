package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.vm.sade.security.ldap.{DirectoryClient, LdapConfig, LdapClient}
import org.scalatra.Cookie

class LoginServlet(directoryClient: DirectoryClient) extends ErrorHandlingServlet {
  post("/") {
    Thread.sleep(100)
    val login = Json.read[Login](request.body)

    val loginResult: Boolean = directoryClient.authenticate(login.username, login.password)

    if(!loginResult) {
      halt(401, reason = "Invalid password or username")
    }
    response.addCookie(Cookie("tor-auth", "balaillaan!"))
    directoryClient.findUser(login.username).map { ldapUser =>
      User(ldapUser.oid, ldapUser.givenNames + " " + ldapUser.lastName)
    } match {
      case Some(user) =>
        Json.write(user)
      case _ =>
        logger.error("User " + login.username + " not found from LDAP")
        halt(401, reason = "User not found")
    }
  }
}
package fi.oph.tor.user

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.CurrentUser
import fi.vm.sade.security.ldap.DirectoryClient


class UserServlet(directoryClient: DirectoryClient, userRepository: UserRepository) extends ErrorHandlingServlet with CurrentUser {
  get("/") {
    getAuthenticatedUser match {
      case Some(user) => Json.write(user)
      case None => halt(401)
    }
  }

  post("/login") {
    val login = Json.read[Login](request.body)

    val loginResult: Boolean = directoryClient.authenticate(login.username, login.password)

    if(!loginResult) {
      halt(401, reason = "Invalid password or username")
    }

    directoryClient.findUser(login.username).map { ldapUser =>
      println(userRepository.getUserOrganisations(ldapUser.oid))

      User(ldapUser.oid, ldapUser.givenNames + " " + ldapUser.lastName)
    } match {
      case Some(user) =>
        request.getSession(true).setAttribute("tor-user", user)
        Json.write(user)
      case _ =>
        logger.error("User " + login.username + " not found from LDAP")
        halt(401, reason = "User not found")
    }
  }

  get("/logout") {
    Option(request.getSession(false)).foreach(_.invalidate())
    response.redirect("/tor")
  }
}
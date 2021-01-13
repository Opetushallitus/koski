package fi.oph.common.koskiuser

import fi.oph.common.log.{LogUserContext, Logging}
import fi.oph.koski.userdirectory.DirectoryClient
import javax.servlet.http.HttpServletRequest

object DirectoryClientLogin extends Logging {
  def findUser(directoryClient: DirectoryClient, request: HttpServletRequest, username: String): Option[AuthenticationUser] = {
    directoryClient.findUser(username).map { ldapUser =>
      AuthenticationUser.fromDirectoryUser(username, ldapUser)
    } match {
      case Some(user) =>
        logger(LogUserContext(request, user.oid, username)).debug("Login successful")
        Some(user)
      case _ =>
        logger(LogUserContext(request)).warn(s"User $username not found")
        None
    }
  }
}

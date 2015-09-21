package fi.oph.tor.security

import fi.oph.tor.user.User
import org.scalatra.ScalatraServlet

trait CurrentUser extends ScalatraServlet {

  def getAuthenticatedUser: Option[User] = {
    Option(request.getSession(false)).flatMap(session => Option(session.getAttribute("tor-user")).asInstanceOf[Option[User]])
  }

}

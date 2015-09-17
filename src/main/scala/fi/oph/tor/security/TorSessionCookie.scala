package fi.oph.tor.security

import fi.oph.tor.user.User

object TorSessionCookie {
  def createSessionCookie(user: User) =
    user.name
  def userFromCookie(cookie: String) =
    User("", cookie)
}

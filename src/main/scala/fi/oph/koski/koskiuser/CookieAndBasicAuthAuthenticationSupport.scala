package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait CookieAndBasicAuthAuthenticationSupport extends AuthenticationSupport {

  def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    userFromCookie match {
      case Right(user) => Right(user)
      case Left(SessionStatusExpiredKansalainen) => Left(KoskiErrorCategory.unauthorized.notAuthenticated())
      case Left(_) => userFromBasicAuth
    }
  }

}

package fi.oph.koski.koskiuser

import fi.oph.koski.http.{ErrorDetail, HttpStatus, KoskiErrorCategory}

trait LuovutuspalveluAuthenticationSupport extends AuthenticationSupport {

  override def getUser: Either[HttpStatus, AuthenticationUser] = {

    // TODO
    /*val userHeader = request.header("user")

    userHeader match {
      case Some(user) => Left(HttpStatus(401, List(ErrorDetail("user", s"user is $user"))))
      case None => Left(KoskiErrorCategory.unauthorized.notAuthenticated())
    }*/

    Left(KoskiErrorCategory.unauthorized.notAuthenticated())

  }

}

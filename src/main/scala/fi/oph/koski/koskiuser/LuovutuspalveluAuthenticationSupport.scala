package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait LuovutuspalveluAuthenticationSupport extends AuthenticationSupport {

  private val clientList = application.luovutuspalveluV2ClientListService.getClientList

  override def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    request.header("x-amzn-mtls-clientcert-subject").map(
      subjectDnHeader =>
        for {
          client <- clientList.find(_.subjectDn == subjectDnHeader).toRight(KoskiErrorCategory.unauthorized("Tuntematon varmenne"))
          clientIp <- request.header("X-Forwarded-For").flatMap(_.split(",").headOption).toRight(KoskiErrorCategory.unauthorized("Tunetematon IP-osoite"))
          _ <- Either.cond(client.ips.contains(clientIp), (), KoskiErrorCategory.unauthorized("Tunetematon IP-osoite"))
          user <- DirectoryClientLogin
            .findUser(application.directoryClient, request, client.user)
            .toRight(KoskiErrorCategory.unauthorized.loginFail())
        } yield user
    ).getOrElse(userFromBasicAuth)
  }
}

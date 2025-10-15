package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait LuovutuspalveluHeaderAuthenticationSupport extends AuthenticationSupport {

  private val clientList = application.luovutuspalveluV2ClientListService.getClientList

  def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    request.header("x-amzn-mtls-clientcert-subject").map(
      subjectDnHeader =>
        for {
          client <- clientList.find(_.subjectDn == subjectDnHeader).toRight {
            // Use defaultLogger to prevent recursion, since we don't have a user yet
            defaultLogger.warn(s"Luovutuspalvelu presented with unknown client certificate ${subjectDnHeader}")
            KoskiErrorCategory.unauthorized("Tuntematon varmenne")
          }
          _ <- Either.cond(client.ips.contains(request.remoteAddress), (), {
            defaultLogger.warn(s"Luovutuspalvelu client ${client.user} connected with unauthorized IP ${request.remoteAddress}")
            KoskiErrorCategory.unauthorized("Tuntematon IP-osoite")
          })
          user <- DirectoryClientLogin
            .findUser(application.directoryClient, request, client.user)
            .toRight(KoskiErrorCategory.unauthorized.loginFail())
        } yield user
    ).getOrElse(userFromBasicAuth)
  }
}

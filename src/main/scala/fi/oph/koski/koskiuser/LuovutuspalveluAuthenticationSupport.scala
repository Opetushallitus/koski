package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import org.scalatra.auth.strategy.BasicAuthStrategy

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

  private def userFromBasicAuth: Either[HttpStatus, AuthenticationUser] = {
    val basicAuthRequest = new BasicAuthStrategy.BasicAuthRequest(request)
    if (basicAuthRequest.isBasicAuth && basicAuthRequest.providesAuth) {
      tryLogin(basicAuthRequest.username, basicAuthRequest.password)
    } else {
      Left(KoskiErrorCategory.unauthorized.notAuthenticated())
    }
  }
}

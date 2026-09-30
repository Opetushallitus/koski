package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait LuovutuspalveluHeaderAuthenticationSupport extends AuthenticationSupport {

  private val clientList = application.luovutuspalveluV2ClientListService.getClientList

  def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    request.header("x-amzn-mtls-clientcert-subject").map(
      subjectDnHeader =>
        for {
          serial <- request.header("x-amzn-mtls-clientcert-serial-number").toRight(KoskiErrorCategory.internalError())
          issuer <- request.header("x-amzn-mtls-clientcert-issuer").toRight(KoskiErrorCategory.internalError())
          _ <- Either.cond(!issuer.endsWith("compute.internal"), (), {
            defaultLogger.error(s"Luovutuspalvelu rejected certificate with disallowed issuer $issuer ($subjectDnHeader, $serial)")
            KoskiErrorCategory.unauthorized("Virheellinen varmenteen myöntäjä")
          })
          client <- clientList.find(_.subjectDn == subjectDnHeader).toRight {
            // Use defaultLogger to prevent recursion, since we don't have a user yet
            defaultLogger.warn(s"Luovutuspalvelu presented with unknown client certificate $subjectDnHeader ($serial)")
            KoskiErrorCategory.unauthorized("Tuntematon varmenne")
          }
          clientIp = LuovutuspalveluClientIp(request)
          _ <- Either.cond(clientIp.exists(client.ips.contains), (), {
            defaultLogger.warn(s"Luovutuspalvelu client ${client.user} connected with unauthorized IP ${clientIp.getOrElse("(none)")}")
            KoskiErrorCategory.unauthorized("Tuntematon IP-osoite")
          })
          user <- DirectoryClientLogin
            .findUser(application.directoryClient, request, client.user)
            .toRight(KoskiErrorCategory.unauthorized.loginFail())
        } yield {
          defaultLogger.info(s"Luovutuspalvelu client certificate $subjectDnHeader ($serial) mapped to user ${user.username}")
          user
        }
    ).getOrElse(Left(KoskiErrorCategory.unauthorized.notAuthenticated()))
  }
}


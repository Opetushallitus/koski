package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait LuovutuspalveluHeaderAuthenticationSupport extends AuthenticationSupport {

  private val clientList = application.luovutuspalveluV2ClientListService.getClientList

  def authenticateUserCandidates: Either[HttpStatus, Seq[AuthenticationUser]] = {
    request.header("x-amzn-mtls-clientcert-subject").map(
      subjectDnHeader =>
        for {
          serial <- request.header("x-amzn-mtls-clientcert-serial-number").toRight(KoskiErrorCategory.internalError())
          issuer <- request.header("x-amzn-mtls-clientcert-issuer").toRight(KoskiErrorCategory.internalError())
          _ <- Either.cond(!issuer.endsWith("compute.internal"), (), {
            defaultLogger.error(s"Luovutuspalvelu rejected certificate with disallowed issuer $issuer ($subjectDnHeader, $serial)")
            KoskiErrorCategory.unauthorized("Virheellinen varmenteen myöntäjä")
          })
          clients <- Some(clientList.filter(_.subjectDn == subjectDnHeader)).filter(_.nonEmpty).toRight {
            // Use defaultLogger to prevent recursion, since we don't have a user yet
            defaultLogger.warn(s"Luovutuspalvelu presented with unknown client certificate $subjectDnHeader ($serial)")
            KoskiErrorCategory.unauthorized("Tuntematon varmenne")
          }
          clientsWithAllowedIp <- Some(clients.filter(_.ips.contains(request.remoteAddress))).filter(_.nonEmpty).toRight {
            defaultLogger.warn(s"Luovutuspalvelu client ${clients.map(_.user).mkString(", ")} connected with unauthorized IP ${request.remoteAddress}")
            KoskiErrorCategory.unauthorized("Tuntematon IP-osoite")
          }
          // Ratkeamaton käyttäjätunnus ei kaada koko pyyntöä, jotta se ei katkaise saman
          // varmenteen muiden tunnusten liikennettä.
          users <- Some(clientsWithAllowedIp.flatMap(client =>
            DirectoryClientLogin.findUser(application.directoryClient, request, client.user)
          ).distinctBy(_.username)).filter(_.nonEmpty).toRight(KoskiErrorCategory.unauthorized.loginFail())
        } yield {
          defaultLogger.info(s"Luovutuspalvelu client certificate $subjectDnHeader ($serial) mapped to user ${users.map(_.username).mkString(", ")}")
          users
        }
    ).getOrElse(Left(KoskiErrorCategory.unauthorized.notAuthenticated()))
  }

  def authenticateUser: Either[HttpStatus, AuthenticationUser] =
    authenticateUserCandidates.flatMap {
      case Seq(user) => Right(user)
      case users =>
        defaultLogger.error(
          s"Luovutuspalvelu client certificate ${request.header("x-amzn-mtls-clientcert-subject").getOrElse("")} " +
            s"maps to several users (${users.map(_.username).mkString(", ")}); only OmaData OAuth2 clients may have more than one"
        )
        Left(KoskiErrorCategory.unauthorized("Varmenteelle on konfiguroitu useita käyttäjätunnuksia"))
    }
}

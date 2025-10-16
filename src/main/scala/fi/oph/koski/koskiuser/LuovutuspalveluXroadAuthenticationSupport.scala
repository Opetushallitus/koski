package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.luovutuspalvelu.{PalveluvaylaServlet, SoapServlet}
import fi.oph.koski.luovutuspalvelu.v2clientlist.XRoadSecurityServer

trait LuovutuspalveluXroadAuthenticationSupport extends AuthenticationSupport with SoapServlet {
  
  private val securityServers: List[XRoadSecurityServer] = application.luovutuspalveluV2XRoadConfigService.getSecurityServers
  private val xroadClients: Map[String, String] = application.luovutuspalveluV2XRoadConfigService.getXRoadClients

  def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    request.header("x-amzn-mtls-clientcert-subject").map(
      subjectDnHeader =>
        for {
          xroadInstance <- securityServers.find(_.subjectDn == subjectDnHeader).toRight {
            // Use defaultLogger to prevent recursion, since we don't have a user yet
            defaultLogger.warn(s"Luovutuspalvelu X-Road security server presented unknown client certificate ${subjectDnHeader}")
            KoskiErrorCategory.unauthorized("Tuntematon varmenne")
          }
          _ <- Either.cond(xroadInstance.ips.contains(request.remoteAddress), (), {
            defaultLogger.warn(s"Luovutuspalvelu X-Road security server connected with unauthorized IP ${request.remoteAddress}")
            KoskiErrorCategory.unauthorized("Tuntematon IP-osoite")
          })
          soap <- xmlBody
          xroadClient <- PalveluvaylaServlet.extractXRoadClient(soap).toRight {
            defaultLogger.warn("Failed to parse client from X-Road security server request")
            KoskiErrorCategory.badRequest("X-Road clientin parsinta epäonnistui")
          }
          clientUsername <- xroadClients.get(xroadClient).toRight {
            defaultLogger.warn(s"Luovutuspalvelu request with unknown X-Road client ${xroadClient}")
            KoskiErrorCategory.unauthorized("Tuntematon X-Road client")
          }
          user <- DirectoryClientLogin
            .findUser(application.directoryClient, request, clientUsername)
            .toRight(KoskiErrorCategory.unauthorized.loginFail())
        } yield user
    ).getOrElse(userFromBasicAuth)
  }
}

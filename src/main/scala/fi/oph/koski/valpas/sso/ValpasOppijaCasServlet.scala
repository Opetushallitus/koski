package fi.oph.koski.valpas.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationUser, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.sso.CasOppijaCreationService
import fi.oph.koski.valpas.servlet.{ValpasApiServlet, ValpasBaseServlet}
import fi.oph.koski.valpas.valpasuser.ValpasAuthenticationSupport

class ValpasOppijaCasServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with ValpasBaseServlet with ValpasAuthenticationSupport with NoCache {
  private val koskiSessions = application.koskiSessionRepository
  private val casService = application.casService
  private val oppijaCreation = new CasOppijaCreationService(application)

  def virhesivu: String = valpasRoot + "/virhe/login"
  def eiTietojaOpintopolussaSivu: String = valpasRoot + "/eitietoja"

  get("/oppija") {
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val hetu = casService.validateKansalainenServiceTicket(casValpasOppijaServiceUrl, ticket)
          oppijaCreation.findOrCreate(request, hetu) match {
            case Some(oppija) =>
              val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
              val user = AuthenticationUser(
                oid = oppija.oid,
                username = oppija.oid,
                name = s"${oppija.etunimet} ${oppija.sukunimi}",
                serviceTicket = Some(ticket),
                kansalainen = true,
                huollettavat = Some(huollettavat),
              )
              koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
              UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, application.directoryClient), response)
              setUser(Right(user))
              redirectAfterLogin

            case None =>
              redirect(eiTietojaOpintopolussaSivu)
          }
        } catch {
          case e: Exception =>
            logger.warn(e)(s"Oppija login ticket validation failed, ${e.toString}")
            redirect(virhesivu)
        }

      case None =>
        logger.warn("Oppija login ticket is missing")
        redirect(virhesivu)
    }
  }
}

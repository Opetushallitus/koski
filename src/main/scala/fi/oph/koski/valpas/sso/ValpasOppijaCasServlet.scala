package fi.oph.koski.valpas.sso

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.OppijaHenkilö
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
  private val oppijaCreation = application.casOppijaCreationService
  private val huoltajaServiceVtj = application.huoltajaServiceVtj
  private val directoryClient = application.directoryClient

  def virhesivu: String = valpasRoot + "/virhe/login"
  def eiTietojaOpintopolussaSivu: String = valpasRoot + "/eitietoja"

  get("/oppija") {
    if (isTestEnvironment) {
      processLoginForTests()
    } else {
      processCasLogin()
    }
  }

  private def isTestEnvironment: Boolean = application.config.getString("login.security") == "mock"

  private def processCasLogin() = {
    params.get("ticket") match {
      case Some(ticket) =>
        try {
          val hetu = casService.validateKansalainenServiceTicket(casValpasOppijaServiceUrl, ticket)
          oppijaCreation.findOrCreate(request, hetu) match {
            case Some(oppija) =>
              val user = toAuthenticationUser(oppija, hetu, Some(ticket))
              koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
              UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, directoryClient), response)
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

  private def processLoginForTests() = {
    request.header("hetu") match {
      case Some(hetu) =>
        oppijaCreation.findOrCreate(request, hetu) match {
          case Some(oppija) =>
            val user = toAuthenticationUser(oppija, hetu, None)
            val mockAuthUser = localLogin(user, Some(langFromCookie.getOrElse("fi")))
            setUser(Right(mockAuthUser))
            redirectAfterLogin
          case None => redirect(virhesivu)
        }
      case None => redirect(virhesivu)
    }
  }

  private def toAuthenticationUser(oppija: OppijaHenkilö, hetu: String, serviceTicket: Option[String]): AuthenticationUser = {
    val huollettavat = application.huoltajaServiceVtj.getHuollettavat(hetu)
    AuthenticationUser(
      oid = oppija.oid,
      username = oppija.oid,
      name = s"${oppija.etunimet} ${oppija.sukunimi}",
      serviceTicket = serviceTicket,
      kansalainen = true,
      huollettavat = Some(huollettavat),
    )
  }
}

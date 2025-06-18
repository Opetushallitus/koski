package fi.oph.koski.valpas.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.koskiuser.{AuthenticationUser, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.servlet.NoCache
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
          val kansalaisenTunnisteet = casService.validateKansalainenServiceTicket(casValpasOppijaServiceUrl, ticket)
          oppijaCreation.findOrCreateByOidOrHetu(request, kansalaisenTunnisteet) match {
            case Some(oppija) =>
              val user = toAuthenticationUser(oppija, oppija.hetu.orElse(kansalaisenTunnisteet.hetu), Some(ticket))
              koskiSessions.store(ticket, user, LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request))
              UserLanguage.setLanguageCookie(UserLanguage.getLanguageFromLDAP(user, directoryClient).getOrElse(UserLanguage.getLanguageFromCookie(request)), response)
              setUser(Right(user))
              redirectAfterLogin

            case None =>
              val nimi = oppijaCreation.nimitiedot(request)
                .map(n => n.etunimet + " " + n.sukunimi)
                .filter(_.trim.nonEmpty)
                .orElse(kansalaisenTunnisteet.nimi)
              setCookie("valpasEiTietojaNimi", nimi.getOrElse(""))
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
            val user = toAuthenticationUser(oppija, Some(hetu), None)
            val mockAuthUser = localLogin(user, Some(langFromCookie.getOrElse("fi")))
            setUser(Right(mockAuthUser))
            redirectAfterLogin
          case None => redirect(virhesivu)
        }
      case None => redirect(virhesivu)
    }
  }

  private def toAuthenticationUser(
    oppija: OppijaHenkilö,
    hetu: Option[String],
    serviceTicket: Option[String]
  ): AuthenticationUser = {
    val huollettavat = hetu.map(huoltajaServiceVtj.getHuollettavat)
    AuthenticationUser(
      oid = oppija.oid,
      username = oppija.oid,
      name = s"${oppija.etunimet} ${oppija.sukunimi}",
      serviceTicket = serviceTicket,
      kansalainen = true,
      huollettavat = huollettavat
    )
  }
}

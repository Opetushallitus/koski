package fi.oph.koski.sso

import java.net.InetAddress.{getAllByName => getInetAddresses, getLocalHost}

import fi.oph.koski.config.Environment.isLocalDevelopmentEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, KoskiSession}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class ShibbolethLoginServlet(application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache{
  get("/") {
    checkAuth.getOrElse(login)
  }

  private def login = {
    request.header("hetu") match {
      case Some(hetu) =>
        application.henkilöRepository.findOppijat(hetu)(KoskiSession.systemUser).headOption match {
          case Some(oppija) =>
            setUser(Right(localLogin(AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true))))
            redirect(s"$rootUrl/omattiedot")
          case _ =>
            haltWithStatus(KoskiErrorCategory.notFound("oppija not found"))
        }
      case _ => haltWithStatus(KoskiErrorCategory.badRequest("hetu header missing"))
    }
  }

  private def checkAuth: Option[HttpStatus] = {
    logger.info(s"Shibboleth login request coming from ${request.getRemoteAddr}")
    if (!originOk) {
      logger.warn(s"Shibboleth login request coming from unauthorized address ${request.getRemoteAddr}")
      return Some(KoskiErrorCategory.unauthorized())
    }

    request.header("security") match {
      case Some(password) if passwordOk(password) => None
      case Some(_) => Some(KoskiErrorCategory.unauthorized())
      case None => Some(KoskiErrorCategory.badRequest("auth header missing"))
    }
  }

  private def originOk =
    localAddresses.contains(request.getRemoteAddr) || request.getRemoteAddr == application.config.getString("shibboleth.ip")

  private lazy val localAddresses =
    getLocalHost.getHostAddress :: getInetAddresses("localhost").toList.distinct.map(_.getHostAddress)

  private def passwordOk(password: String) = {
    val security = application.config.getString("shibboleth.security")
    if (!isLocalDevelopmentEnvironment && (security.isEmpty || security == "mock")) {
      false
    } else {
      password == security
    }
  }

  private def rootUrl =
    if (isLocalDevelopmentEnvironment) ""
    else application.config.getString("koski.root.url")
}
package fi.oph.koski.sso

import fi.oph.koski.config.Environment.isLocalDevelopmentEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, KoskiSession}
import fi.oph.koski.schema.Nimitiedot
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class ShibbolethLoginServlet(application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache{
  get("/") {
    checkAuth.getOrElse(login)
  }

  private def checkAuth: Option[HttpStatus] = {
    logger.debug(s"Shibboleth login request coming from ${request.remoteAddress}")
    logger.debug(s"${request.headers.toList.sortBy(_._1).mkString("\n")}")
    request.header("security") match {
      case Some(password) if passwordOk(password) => None
      case Some(_) => Some(KoskiErrorCategory.unauthorized())
      case None => Some(KoskiErrorCategory.badRequest("auth header missing"))
    }
  }

  private def login = {
    hetu match {
      case Right(hetu) =>
        application.henkilöRepository.findHenkilötiedotByHetu(hetu, nimitiedot)(KoskiSession.systemUser).headOption match {
          case Some(oppija) =>
            setUser(Right(localLogin(AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true))))
            redirect(s"$rootUrl/omattiedot")
          case _ =>
            redirect(s"$rootUrl/eisuorituksia")
        }
      case Left(status) => haltWithStatus(status)
    }
  }

  private def hetu: Either[HttpStatus, String] = {
    request.header("hetu").map(Hetu.validate(_, acceptSynthetic = true)).getOrElse(Left(KoskiErrorCategory.badRequest("hetu header missing")))
  }

  private def nimitiedot: Option[Nimitiedot] = {
    def toNimitiedot(cn: String) = {
      val nimet = cn.split(" ")
      Nimitiedot(nimet.tail.mkString(" "), nimet.tail.headOption.getOrElse(""), nimet.head)
    }
    request.header("cn").map(_.trim).filter(_.nonEmpty).map(toNimitiedot)
  }

  private def passwordOk(password: String) = {
    val security = application.config.getString("shibboleth.security")
    if (!isLocalDevelopmentEnvironment && (security.isEmpty || security == "mock")) {
      false
    } else {
      password == security
    }
  }

  private def rootUrl = {
    val endsInSlash = """/$""".r
    endsInSlash.replaceAllIn(application.config.getString("koski.oppija.root.url"), "")
  }
}

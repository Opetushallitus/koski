package fi.oph.koski.sso

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.writeWithRoot
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, KoskiSession}
import fi.oph.koski.schema.Nimitiedot
import fi.oph.koski.servlet.{ApiServlet, LanguageSupport, NoCache}
import org.scalatra.{Cookie, CookieOptions}

case class ShibbolethLoginServlet(application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache with LanguageSupport {
  get("/") {
    try {
      checkAuth.getOrElse(login)
    } catch {
      case e: Exception =>
        logger.error(s"Kansalaisen sisäänkirjautuminen epäonnistui ${e.getMessage}")
        redirect("/virhesivu")
    }
  }

  private def checkAuth: Option[HttpStatus] = {
    logger.debug(headers)
    request.header("security") match {
      case Some(password) if passwordOk(password) => None
      case Some(_) => Some(KoskiErrorCategory.unauthorized())
      case None => Some(KoskiErrorCategory.badRequest("auth header missing"))
    }
  }

  private def login = {
    application.henkilöRepository.findHenkilötiedotByHetu(hetu, nimitiedot)(KoskiSession.systemUser).headOption match {
      case Some(oppija) =>
        setUser(Right(localLogin(AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true), Some(langFromCookie.getOrElse(langFromDomain)))))
        redirect("/omattiedot")
      case _ =>
        setNimitiedotCookie
        redirect("/eisuorituksia")
    }
  }

  private def setNimitiedotCookie = {
    val shibbolethName = nimitiedot.map(n => ShibbolethName(name = n.etunimet + " " + n.sukunimi))
    response.addCookie(Cookie("eisuorituksia", encode(writeWithRoot(shibbolethName), "UTF-8"))(CookieOptions(secure = isHttps, path = "/", maxAge = application.sessionTimeout.seconds, httpOnly = true)))
  }

  private def hetu: String =
    request.header("hetu").map(Hetu.validate(_, acceptSynthetic = true)).getOrElse(Left(KoskiErrorCategory.badRequest("hetu header missing"))) match {
      case Right(hetu) => hetu
      case Left(status) => throw new Exception(status.toString)
    }

  private def nimitiedot: Option[Nimitiedot] = {
    val nimi = for {
      etunimet <- utf8Header("FirstName")
      kutsumanimi <- utf8Header("givenName")
      sukunimi <- utf8Header("sn")
    } yield Nimitiedot(etunimet = etunimet, kutsumanimi = kutsumanimi, sukunimi = sukunimi)
    logger.debug(nimi.toString)
    nimi
  }

  private def utf8Header(headerName: String): Option[String] =
    request.header(headerName)
      .map(header => new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
      .map(_.trim)
      .filter(_.nonEmpty)

  private def passwordOk(password: String) = {
    val security = application.config.getString("shibboleth.security")
    if (security.isEmpty) {
      false
    } else {
      password == security
    }
  }

  private val sensitiveHeaders = List("security", "hetu")
  private val headersWhiteList = List("FirstName", "cn", "givenName", "hetu", "oid", "security", "sn")
  private def headers: String = {
    request.headers.toList.collect { case (name, value) if headersWhiteList.contains(name) =>
      if (sensitiveHeaders.contains(name)) {
        (name, "*********")
      } else {
        (name, value)
      }
    }.sortBy(_._1).mkString("\n")
  }
}

case class ShibbolethName(name: String)

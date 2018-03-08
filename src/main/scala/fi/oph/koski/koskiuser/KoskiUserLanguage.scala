package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest
import fi.oph.koski.log.Logging
import fi.oph.koski.userdirectory.DirectoryClient
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

object KoskiUserLanguage extends Logging {
  def getLanguageFromLDAP(user: AuthenticationUser, directoryClient: DirectoryClient) = {
    val username = user.username
    directoryClient.findUser(username) match {
      case Some(ldapUser) =>
        ldapUser.asiointikieli.map(_.toLowerCase).getOrElse("fi")
      case _ =>
        if (!user.kansalainen) {
          logger.warn(s"User $username not found")
        }
        "fi"
    }
  }

  def getLanguageFromCookie(request: RichRequest) = request.cookies.getOrElse("lang", "fi")

  def setLanguageCookie(lang: String, response: RichResponse) = {
    response.addCookie(Cookie("lang", lang)(CookieOptions(path = "/")))
  }
}

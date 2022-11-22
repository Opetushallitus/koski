package fi.oph.koski.koskiuser

import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.userdirectory.DirectoryClient
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

object UserLanguage extends Logging {
  def getLanguageFromLDAP(user: AuthenticationUser, directoryClient: DirectoryClient): String = {
    val username = user.username
    directoryClient.findUser(username) match {
      case Some(ldapUser) =>
        sanitizeLanguage(ldapUser.asiointikieli).getOrElse("fi")
      case _ =>
        if (!user.kansalainen) {
          logger.warn(s"User $username not found")
        }
        "fi"
    }
  }

  def getLanguageFromCookie(request: RichRequest): String = sanitizeLanguage(request.cookies.get("lang")).getOrElse("fi")

  def setLanguageCookie(lang: String, response: RichResponse): Unit = {
    response.addCookie(Cookie("lang", lang)(CookieOptions(path = "/")))
  }

  def sanitizeLanguage(possibleLanguage: Option[String]): Option[String] = {
    possibleLanguage
      .map(_.toLowerCase)
      .filter(LocalizedString.languages.contains)
  }
}

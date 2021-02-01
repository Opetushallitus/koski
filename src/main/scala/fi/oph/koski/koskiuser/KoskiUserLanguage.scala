package fi.oph.koski.koskiuser

import fi.oph.common.log.Logging
import fi.oph.common.schema.LocalizedString
import fi.oph.koski.userdirectory.DirectoryClient
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

object KoskiUserLanguage extends Logging {
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
      .filterNot(_ == "en") // can be removed when our UI actually supports English
  }
}

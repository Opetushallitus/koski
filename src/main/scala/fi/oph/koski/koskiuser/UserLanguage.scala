package fi.oph.koski.koskiuser

import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.userdirectory.DirectoryClient
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

import scala.util.{Failure, Success, Try}

object UserLanguage extends Logging {
  // Request-attribuutti, johon tälle pyynnölle ratkaistu kieli talletetaan silloin, kun lang-eväste päivittyy.
  // Tarvitaan, koska vastaukseen asetettu eväste ei näy vielä saman pyynnön request.cookiesissa.
  val LangAttribute = "koskiResolvedLang"

  def getLanguageFromUserDirectory(user: AuthenticationUser, directoryClient: DirectoryClient): Option[String] = {
    val username = user.username
    directoryClient.findUser(username) match {
      case Some(directoryUser) =>
        sanitizeLanguage(directoryUser.asiointikieli)
      case _ =>
        if (!user.kansalainen) {
          logger.warn(s"User $username not found")
        }
        None
    }
  }

  def getLanguageFromCookie(request: RichRequest): String = sanitizeLanguage(request.cookies.get("lang")).getOrElse("fi")

  def setLanguageCookie(lang: String, response: RichResponse): Unit = {
    response.addCookie(Cookie("lang", lang)(CookieOptions(path = "/")))
  }

  def removeLanguageCookie(response: RichResponse): Unit = {
    response.addCookie(Cookie("lang", "")(CookieOptions(path = "/", maxAge = 0)))
  }

  /**
   * Päivitetään virkailijan kieli myös olemassa olevan evästeen tapauksessa, jotta henkilo-ui/omattiedot-
   * palvelussa tallennettu asiointikieli näkyy seuraavalla sivulatauksella. Kielihaku ohittaa käyttäjävälimuistin.
   * Jos haku epäonnistuu tai kieli puuttuu, säilytetään nykyinen eväste ja yritetään seuraavalla latauksella.
   * Palautetaan muuttunut kieli saman pyynnön HTML-renderöintiä varten.
   */
  def setLanguageCookieFromUserIfNecessary(
    user: AuthenticationUser,
    directoryClient: DirectoryClient,
    request: RichRequest,
    response: RichResponse
  ): Option[String] = {
    // Kansalaisen kieli päätellään domainista, ks. LanguageSupport.setLangCookieFromDomainIfNecessary
    if (user.kansalainen) {
      None
    } else {
      Try(sanitizeLanguage(directoryClient.findAsiointikieli(user))) match {
        case Success(Some(lang)) if !sanitizeLanguage(request.cookies.get("lang")).contains(lang) =>
          setLanguageCookie(lang, response)
          Some(lang)
        case Success(_) =>
          None
        case Failure(e) =>
          logger.warn(e)(s"Käyttäjän ${user.username} asiointikielen haku epäonnistui, lang-evästettä ei aseteta")
          None
      }
    }
  }

  def sanitizeLanguage(possibleLanguage: Option[String]): Option[String] = {
    possibleLanguage
      .map(_.toLowerCase)
      .filter(LocalizedString.languages.contains)
  }
}

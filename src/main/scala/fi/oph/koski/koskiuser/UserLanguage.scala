package fi.oph.koski.koskiuser

import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.userdirectory.DirectoryClient
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

import scala.util.{Failure, Success, Try}

object UserLanguage extends Logging {
  // Request-attribuutti, johon tälle pyynnölle ratkaistu kieli talletetaan silloin, kun lang-evästettä ei vielä ollut.
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
   * Virkailijan asiointikieli ratkaistaan vain CAS-tiketin validoinnin yhteydessä. Koska lang on istuntoeväste ja
   * koskiUser pysyvä eväste, selaimen sulkeminen hukkaa kielen mutta säilyttää istunnon. Tällöin istunto jatkuu
   * ilman uutta tikettiä, jolloin kieltä ei haeta enää koskaan uudelleen ja käyttöliittymä jää suomeksi.
   * Täydennetään puuttuva eväste tässä.
   *
   * Palauttaa ratkaistun kielen vain jos eväste puuttui ja haku onnistui. Jos haku epäonnistuu tai asiointikieltä
   * ei ole, evästettä EI aseteta: muuten ohimenevästä virheestä tulisi pysyvä, koska eväste olisi jatkossa olemassa
   * eikä tämä täydennys enää laukeaisi.
   */
  def setLanguageCookieFromUserIfNecessary(
    user: AuthenticationUser,
    directoryClient: DirectoryClient,
    request: RichRequest,
    response: RichResponse
  ): Option[String] = {
    // Kansalaisen kieli päätellään domainista, ks. LanguageSupport.setLangCookieFromDomainIfNecessary
    if (user.kansalainen || sanitizeLanguage(request.cookies.get("lang")).isDefined) {
      None
    } else {
      Try(getLanguageFromUserDirectory(user, directoryClient)) match {
        case Success(Some(lang)) =>
          setLanguageCookie(lang, response)
          Some(lang)
        case Success(None) =>
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

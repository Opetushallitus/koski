package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.userdirectory.DirectoryClient
import jakarta.servlet.http.HttpServletRequest
import org.scalatra.servlet.{RichRequest, RichResponse}
import org.scalatra.{Cookie, CookieOptions}

import scala.util.{Failure, Success, Try}

/**
 * Kielellä on kaksi eri omistajaa, eikä niitä pidä sotkea samaan tilaan:
 *
 *  - Virkailijan kieli on asiointikieli oppijanumerorekisteristä. Se ratkaistaan palvelimella
 *    jokaisella pyynnöllä (haku on välimuistitettu, ks. DirectoryClient) eikä sitä kirjoiteta
 *    evästeeseen lainkaan. Frontend saa ratkaistun kielen sivun mukana, ks. HtmlNodes ja
 *    ValpasBootstrapServlet.
 *  - Kansalaisen kieli on käyttäjän oma valinta, jonka vain käyttäjä asettaa (kielivalitsimet
 *    ChangeLang, SuoritusjakoTopBar) ja joka säilyy lang-evästeessä. Oletus tulee domainista.
 *    Samaa evästettä lukee myös oppijan raamit.
 */
object UserLanguage extends Logging {
  private val LangCookie = "lang"

  // Virkailijan sivut tarjoillaan vain virkailija-domainista, joten domainista ei voi päätellä
  // hänen kieltään: ilman asiointikieltä käytetään oletusta.
  val DefaultLanguage = "fi"

  // Sivunlatauksen ratkaisema kieli muistetaan pyynnön ajaksi: sama pyyntö kysyy kieltä monta
  // kertaa (html lang, käännökset), eikä ONR:ää ole syytä kutsua kuin kerran.
  val LangAttribute = "koskiResolvedLang"

  /** Sessioon ratkaistu kieli: välimuistin läpi, koska sessio luodaan joka pyynnössä. */
  def resolveLanguage(
    user: AuthenticationUser,
    directoryClient: DirectoryClient,
    request: HttpServletRequest,
    config: Config
  ): String = resolve(user, request, config)(directoryClient.findAsiointikieli)

  /**
   * Sivunlatauksen kieli: ohi välimuistin, jotta virkailijan henkilo-ui:ssa tekemä asiointikielen
   * vaihto näkyy heti seuraavassa latauksessa.
   */
  def resolveLanguageFresh(
    user: AuthenticationUser,
    directoryClient: DirectoryClient,
    request: HttpServletRequest,
    config: Config
  ): String = resolve(user, request, config)(directoryClient.findAsiointikieliUncached)

  private def resolve(
    user: AuthenticationUser,
    request: HttpServletRequest,
    config: Config
  )(findAsiointikieli: AuthenticationUser => Option[String]): String =
    if (user.kansalainen) {
      languageFromCookieOrDomain(request, config)
    } else {
      Try(sanitizeLanguage(findAsiointikieli(user))) match {
        case Success(Some(lang)) => lang
        case Success(None) => DefaultLanguage
        case Failure(e) =>
          logger.warn(e)(s"Käyttäjän ${user.username} asiointikielen haku epäonnistui, käytetään oletuskieltä")
          DefaultLanguage
      }
    }

  def languageFromCookieOrDomain(request: HttpServletRequest, config: Config): String =
    languageFromCookie(request).getOrElse(languageFromDomain(request, config))

  // Kansalaisen omat sivut ja suoritusjaot toimivat myös ilman kirjautumista, jolloin kieli on
  // pelkästään selaimen valinta.
  def languageFromCookie(request: RichRequest): Option[String] =
    sanitizeLanguage(request.cookies.get(LangCookie))

  def languageFromCookie(request: HttpServletRequest): Option[String] =
    sanitizeLanguage(
      Option(request.getCookies).toList.flatten.find(_.getName == LangCookie).map(_.getValue)
    )

  def languageFromDomain(request: HttpServletRequest, config: Config): String =
    if (request.getServerName == config.getString("koski.oppija.domain.sv")) {
      "sv"
    } else if (request.getServerName == config.getString("koski.oppija.domain.en")) {
      "en"
    } else {
      DefaultLanguage
    }

  def setLanguageCookie(lang: String, response: RichResponse): Unit =
    response.addCookie(Cookie(LangCookie, lang)(CookieOptions(path = "/")))

  def removeLanguageCookie(response: RichResponse): Unit =
    response.addCookie(Cookie(LangCookie, "")(CookieOptions(path = "/", maxAge = 0)))

  def sanitizeLanguage(possibleLanguage: Option[String]): Option[String] =
    possibleLanguage
      .map(_.toLowerCase)
      .filter(LocalizedString.languages.contains)
}

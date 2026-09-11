package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.koskiuser.{AuthenticationUser, UserLanguage}
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.ValpasCookieAndBasicAuthAuthenticationSupport

import java.net.URLDecoder

class ValpasBootstrapServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with ValpasCookieAndBasicAuthAuthenticationSupport {
  get("/window-properties") {
    WindowProperties(
      valpasLocalizationMap = application.valpasLocalizationRepository.localizations,
      // SPA:n käynnistys vastaa sivunlatausta, joten kieli haetaan ohi välimuistin.
      valpasLang = getUser.toOption
        .map(user => UserLanguage.resolveLanguageFresh(
          user, application.directoryClient, request, application.config))
        // Ilman sessiota asiointikieltä ei ole mistä ratkaista (esim. Valppaan kirjautumissivu),
        // joten kieli on selaimen oma valinta ja oletuksena domain. Kirjautuneen virkailijan kieli
        // ei tule koskaan evästeestä. Vrt. LanguageSupport.lang.
        .getOrElse(UserLanguage.languageFromCookieOrDomain(request, application.config)),
      environment = Environment.currentEnvironment(application.config),
      opintopolkuVirkailijaUrl = application.config.getString("opintopolku.virkailija.url"),
      opintopolkuOppijaUrl = application.config.getString("opintopolku.oppija.url"),
      oppijaRaamitUser = getUser.map(OppijaRaamitUser.apply).toOption.orElse(getUserWithNameFromCookie),
    )
  }

  private def getUserWithNameFromCookie: Option[OppijaRaamitUser] = request.cookies.get("valpasEiTietojaNimi")
    .map(c => URLDecoder.decode(c, "UTF-8"))
    .map(_.replace("\"", ""))
    .map(name => OppijaRaamitUser(name, ""))
}

case class WindowProperties(
  valpasLocalizationMap: Map[String, LocalizedString],
  valpasLang: String,
  environment: String,
  opintopolkuVirkailijaUrl: String,
  opintopolkuOppijaUrl: String,
  oppijaRaamitUser: Option[OppijaRaamitUser],
)

case class OppijaRaamitUser(
  name: String,
  oid: String,
)

object OppijaRaamitUser {
  def apply(user: AuthenticationUser): OppijaRaamitUser = OppijaRaamitUser(
    name = user.name,
    oid = user.oid,
  )
}

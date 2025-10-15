package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.koskiuser.AuthenticationUser
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.ValpasCookieAndBasicAuthAuthenticationSupport

import java.net.URLDecoder

class ValpasBootstrapServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with ValpasCookieAndBasicAuthAuthenticationSupport {
  get("/window-properties") {
    WindowProperties(
      valpasLocalizationMap = application.valpasLocalizationRepository.localizations,
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

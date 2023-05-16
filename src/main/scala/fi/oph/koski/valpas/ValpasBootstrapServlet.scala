package fi.oph.koski.valpas

import fi.oph.koski.config.{AppConfig, Environment, KoskiApplication}
import fi.oph.koski.koskiuser.AuthenticationUser
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.ValpasAuthenticationSupport

class ValpasBootstrapServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with ValpasAuthenticationSupport {
  get("/window-properties") {
    WindowProperties(
      valpasLocalizationMap = application.valpasLocalizationRepository.localizations,
      environment = Environment.currentEnvironment(application.config),
      opintopolkuVirkailijaUrl = AppConfig.virkailijaOpintopolkuUrl(application.config).getOrElse("mock"),
      opintopolkuOppijaUrl = AppConfig.oppijaOpintopolkuUrl(application.config),
      oppijaRaamitUser = getUser.map(OppijaRaamitUser.apply).toOption,
    )
  }
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

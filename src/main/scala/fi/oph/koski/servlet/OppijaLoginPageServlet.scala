package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import fi.oph.koski.util.JsStringInterpolation.setWindowVar
import org.scalatra.ScalatraServlet
import fi.oph.koski.xml.NodeSeqImplicits._

class OppijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with KoskiSpecificSSOSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/")(nonce => {
    redirectToOppijaLogin
  })

  get("/local")(nonce => {
    val security = application.config.getString("login.security")
    if(security != "mock") {
      haltWithStatus(KoskiErrorCategory.unauthorized())
    }

    htmlIndex(
      scriptBundleName = "koski-korhopankki.js",
      scripts = Seq(<script nonce={nonce} id="auth">{setWindowVar("mockUsers", oppijat)}</script>),
      responsive = true,
      nonce = nonce
    )
  })

  private def oppijat =
    application.fixtureCreator
      .defaultOppijat
      .filter(_.henkilö.hetu.isDefined)
      .sortBy(_.henkilö.etunimet)
      .map(h => KorhopankkiOppija(h.henkilö.hetu, s"${h.henkilö.etunimet} ${h.henkilö.sukunimi}"))
}

case class KorhopankkiOppija(
  hetu: Option[String],
  nimi: String,
)

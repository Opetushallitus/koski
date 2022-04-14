package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import fi.oph.koski.util.Cryptographic
import fi.oph.koski.util.JsStringInterpolation.setWindowVar
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class OppijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with KoskiSpecificSSOSupport {
  get("/") {
    redirectToOppijaLogin
  }

  get("/local") {
    val security = application.config.getString("login.security")
    if(security != "mock") {
      haltWithStatus(KoskiErrorCategory.unauthorized())
    }

    val nonce = Cryptographic.nonce

    htmlIndex(
      scriptBundleName = "koski-korhopankki.js",
      scripts = <script nonce={nonce} id="auth">{setWindowVar("mockUsers", oppijat)}</script>,
      responsive = true,
      nonce = nonce
    )
  }

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

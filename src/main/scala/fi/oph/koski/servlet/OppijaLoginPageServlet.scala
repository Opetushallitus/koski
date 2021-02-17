package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class OppijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with KoskiSpecificSSOSupport {
  get("/") {
    redirectToOppijaLogin
  }

  get("/local") {
    htmlIndex(
      scriptBundleName = "koski-korhopankki.js",
      scripts = <script id="auth">{Unparsed(s"window.mockUsers=$oppijat")}</script>,
      responsive = true
    )
  }

  private def oppijat = application.fixtureCreator.defaultOppijat.sortBy(_.henkilö.etunimet).flatMap { o =>
    o.henkilö.hetu.filter(_.nonEmpty).map(h => s"""{'hetu': '$h', 'nimi': '${o.henkilö.etunimet} ${o.henkilö.sukunimi}'}""")
  }.distinct.mkString("[", ",", "]")
}

package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication, ShibbolethSecret}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.sso.SSOSupport
import org.scalatra.{EnvironmentKey, ScalatraServlet}

import scala.xml.Unparsed

class OppijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with SSOSupport {
  get("/") {
    val shibbolethSecurity = if (Environment.usesAwsSecretsManager) {
      ShibbolethSecret.fromSecretsManager
    } else {
      ShibbolethSecret.fromConfig(application.config)
    }
    if (shibbolethSecurity == "mock") {
      htmlIndex(
        scriptBundleName = "koski-korhopankki.js",
        scripts = <script id="auth">{Unparsed(s"window.mockUsers=$oppijat")}</script>,
        responsive = true
      )
    } else {
      logger.error("Mock shibboleth in use, please check shibboleth.url config")
      haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  private def oppijat = MockOppijat.defaultOppijat.sortBy(_.henkilö.etunimet).flatMap { o =>
    o.henkilö.hetu.filter(_.nonEmpty).map(h => s"""{'hetu': '$h', 'nimi': '${o.henkilö.etunimet} ${o.henkilö.sukunimi}'}""")
  }.distinct.mkString("[", ",", "]")
}

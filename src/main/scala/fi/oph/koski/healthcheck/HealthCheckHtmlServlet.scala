package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet

class HealthCheckHtmlServlet(implicit val application: KoskiApplication) extends HtmlServlet{
  get("/") {
    val healthcheck = application.healthCheck.healthcheck
    val version = buildVersionProperties.map(_.getProperty("version", null)).filter(_ != null).getOrElse("local")
    val buildDate = buildVersionProperties.map(_.getProperty("buildDate", null)).filter(_ != null).getOrElse("")

    val status = healthcheck.isOk match {
      case true =>
        <div style="height: 100%; background: #528a1e; background: linear-gradient(to bottom, darkgreen 0%,green 100%); color: white; font-family: sans-serif;">
          <h1 style="font-size: 20vw; margin-bottom: 4vw;">OK</h1>
          <div style="font-size: 4vw; padding-bottom: 4vw;">{version} — {buildDate}</div>
        </div>
      case false =>
        <div style="height: 100%; background: red;">
          <h1 style="font-size: 20vw; margin-bottom: 0;">FAIL</h1>
          <ul style="font-size: 4vw; list-style: none; line-height: 5vw; padding-left: 0;">
            {healthcheck.errors.map(_.key).distinct.map(e => <li>{e}</li>)}
          </ul>
          <div style="font-size: 4vw; padding-bottom: 4vw;">{version} — {buildDate}</div>
        </div>
    }
    <html style="text-align: center;">
      {status}
    </html>
  }
}

package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.html.ContentSecurityPolicy
import fi.oph.koski.servlet.VirkailijaHtmlServlet
import fi.oph.koski.util.Cryptographic

class HealthCheckHtmlServlet(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet{
  get("/") {
    val nonce = Cryptographic.nonce

    val healthcheck = application.healthCheck.healthcheckWithExternalSystems
    val version = buildVersionProperties.map(_.getProperty("version", null)).filter(_ != null).getOrElse("local")
    val buildDate = buildVersionProperties.map(_.getProperty("buildDate", null)).filter(_ != null).getOrElse("")

    // TODO: nämä inline style:t tuskin toimiva ilman unsafe-inline -csp:tä.

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
    <html lang={lang} style="text-align: center;">
      <head>
        {ContentSecurityPolicy.create(nonce)}
      </head>
      {status}
    </html>
  }
}

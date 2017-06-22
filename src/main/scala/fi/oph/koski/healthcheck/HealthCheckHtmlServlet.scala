package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet

class HealthCheckHtmlServlet(val application: KoskiApplication) extends HtmlServlet{
  get("/") {
    val healthcheck = application.healthCheck.healthcheck
    val status = healthcheck.isOk match {
      case true => <h1 style="font-size: 20vw; background: #528a1e; background: linear-gradient(to bottom, darkgreen 0%,green 100%); color: white; font-family: sans-serif; border-radius: 3px;">OK</h1>
      case false =>
        <div style="background: red;">
          <h1 style="font-size: 20vw; margin-bottom: 0;">FAIL</h1>
          <ul style="list-style: none; line-height: 25px; padding-bottom: 50px">
            {healthcheck.errors.map(_.key).distinct.map(e => <li>{e}</li>)}
          </ul>
        </div>
    }
    <html style="text-align: center;">
      {status}
    </html>
  }
}

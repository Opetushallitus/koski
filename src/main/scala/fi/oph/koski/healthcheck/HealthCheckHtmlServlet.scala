package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet

class HealthCheckHtmlServlet(val application: KoskiApplication) extends HtmlServlet{
  get() {
    val status = HeathChecker(application).healthcheck.isOk match {
      case true => <h1 style="font-size: 20vw; background: #528a1e; background: linear-gradient(to bottom, darkgreen 0%,green 100%); color: white; font-family: sans-serif; border-radius: 3px;">OK</h1>
      case false => <h1 style="font-size: 20vw; background: red;">FAIL</h1>
    }
    <html style="text-align: center;">
      <h1>{status}</h1>
    </html>
  }
}

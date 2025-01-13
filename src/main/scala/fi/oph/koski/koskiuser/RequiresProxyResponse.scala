package fi.oph.koski.koskiuser

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.servlet.KoskiSpecificBaseServlet

trait RequiresProxyResponse extends KoskiSpecificBaseServlet {
  implicit val application: KoskiApplication
  before() {
    request.getHeader("proxyResponse") match {
      case "proxied" => // proceed
      case _ if Environment.isProdEnvironment(application.config) => halt(403, "Forbidden: Missing or invalid proxyResponse header")
      case _ => // proceed
    }
  }
}

package fi.oph.koski.servlet

import fi.oph.koski.html.Raamit
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiCookieAndBasicAuthenticationSupport
import fi.oph.koski.log.Logging
import fi.oph.koski.util.XML

import java.net.URI
import scala.xml.Elem

trait KoskiHtmlServlet extends HtmlServlet with KoskiCookieAndBasicAuthenticationSupport with Logging {

  def renderStatus(status: HttpStatus): Unit = {
    // Generoi nonce tässä virheilmoitussivua varten erikseen, koska virhetilanteissa voidaan päätyä rendaamaan
    // HTML-virheilmoitusta myös suoraan, esim. käyttäjän kirjoittaessa URL-osoitteen väärin.
    val nonce = setNonceHeader
    renderStatus(status, nonce)
  }

  def renderStatus(status: HttpStatus, nonce: String): Unit = {
    val html = XML.transform(htmlIndex("koski-main.js", raamit = virkailijaRaamit, nonce = nonce)) {
      case e: Elem if e.label == "head" =>
        e copy (child = e.child :+ htmlErrorObjectScript(nonce, status))
    }

    response.setStatus(status.statusCode)
    renderHtml(html)
  }

  def subdomainRedirectNeeded: Option[String] = {
    val url = URI.create(request.getRequestURL.toString)
    """(?<subdomain>\w+)\.(?<rest>.+)""".r.findFirstMatchIn(url.getHost) match {
      case Some(m) if m.group("subdomain") == "koski" =>
        Some(new URI(
          url.getScheme,
          null,
          m.group("rest"),
          url.getPort,
          url.getPath,
          null,
          null,
        ).toString)
      case _ => None
    }
  }


  protected val virkailijaRaamitSet: Boolean

  protected def virkailijaRaamit: Raamit
}

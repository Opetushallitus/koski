package fi.oph.koski.servlet

import fi.oph.koski.html.Raamit
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.util.{Cryptographic, XML}

import scala.xml.Elem

trait KoskiHtmlServlet extends HtmlServlet with KoskiSpecificAuthenticationSupport {

  def renderStatus(status: HttpStatus): Unit = {
    val nonce = Cryptographic.nonce
    val html = XML.transform(htmlIndex("koski-main.js", piwikHttpStatusCode = Some(status.statusCode), raamit = virkailijaRaamit, nonce = nonce)) {
      case e: Elem if e.label == "head" =>
        e copy (child = (e.child :+ htmlErrorObjectScript(nonce, status)) ++ piwikTrackErrorObject(nonce))
    }

    response.setStatus(status.statusCode)
    renderHtml(html)
  }

  protected val virkailijaRaamitSet: Boolean

  protected def virkailijaRaamit: Raamit
}

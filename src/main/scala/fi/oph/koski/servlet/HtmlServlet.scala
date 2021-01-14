package fi.oph.koski.servlet

import java.util.Properties

import fi.oph.koski.config.Environment
import fi.oph.koski.html.{EiRaameja, HtmlNodes, Raamit, Virkailija}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.util.XML

import scala.reflect.runtime.{universe => ru}
import scala.util.Try
import scala.xml.Elem

trait HtmlServlet extends KoskiBaseServlet with AuthenticationSupport with HtmlNodes {

  lazy val buildVersionProperties = Option(getServletContext.getResourceAsStream("/buildversion.txt")).map { i =>
    val p = new Properties()
    p.load(i)
    p
  }

  lazy val buildVersion: Option[String] = buildVersionProperties.map(_.getProperty("vcsRevision", null))

  lazy val piwikSiteId: String = application.config.getString("piwik.siteId")

  override def haltWithStatus(status: HttpStatus): Nothing = status.statusCode match {
    case 401 => redirectToVirkailijaLogin
    case _ => super.haltWithStatus(status)
  }

  def renderStatus(status: HttpStatus): Unit = {
    val html = XML.transform(htmlIndex("koski-main.js", piwikHttpStatusCode = Some(status.statusCode), raamit = virkailijaRaamit)) {
      case e: Elem if e.label == "head" =>
        e copy (child = (e.child :+ htmlErrorObjectScript(status)) ++ piwikTrackErrorObject)
    }

    response.setStatus(status.statusCode)
    renderHtml(html)
  }

  override def renderObject[T: ru.TypeTag](x: T) = x match {
    case e: Elem =>
      renderHtml(e)
    case _ =>
      logger.error("HtmlServlet cannot render " + x)
      renderStatus(KoskiErrorCategory.internalError())
  }

  def virkailijaRaamitSet: Boolean
  def virkailijaRaamit: Raamit
}

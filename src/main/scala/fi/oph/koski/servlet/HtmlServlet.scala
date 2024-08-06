package fi.oph.koski.servlet

import fi.oph.koski.frontendvalvonta.FrontendValvottuServlet

import java.util.Properties
import fi.oph.koski.html.HtmlNodes
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AuthenticationSupport

import scala.reflect.runtime.{universe => ru}
import scala.xml.Elem

trait HtmlServlet extends KoskiSpecificBaseServlet with AuthenticationSupport with HtmlNodes with FrontendValvottuServlet {
  lazy val buildVersionProperties = Option(getServletContext.getResourceAsStream("/buildversion.txt")).map { i =>
    val p = new Properties()
    p.load(i)
    p
  }

  lazy val buildVersion: Option[String] = buildVersionProperties.map(_.getProperty("vcsRevision", null))

  override def haltWithStatus(status: HttpStatus): Nothing = status.statusCode match {
    case 401 => redirectToVirkailijaLogin
    case _ => super.haltWithStatus(status)
  }

  override def renderObject[T: ru.TypeTag](x: T) = x match {
    case e: Elem =>
      renderHtml(e)
    case _ =>
      logger.error("HtmlServlet cannot render " + x)
      renderStatus(KoskiErrorCategory.internalError())
  }
}

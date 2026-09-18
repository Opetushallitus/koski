package fi.oph.koski.servlet

import fi.oph.koski.frontendvalvonta.FrontendValvottuServlet
import fi.oph.koski.html.HtmlNodes
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.util.BuildVersion

import scala.reflect.runtime.{universe => ru}
import scala.xml.Elem

trait HtmlServlet extends KoskiSpecificBaseServlet with AuthenticationSupport with HtmlNodes with FrontendValvottuServlet {
  protected lazy val buildMetadata: Option[BuildVersion] = BuildVersion.read()
  lazy val buildVersion: Option[String] = buildMetadata.flatMap(_.vcsRevision)

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

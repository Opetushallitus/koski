package fi.oph.koski.servlet

import java.util.Properties

import fi.oph.koski.html.HtmlNodes
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AuthenticationSupport

import scala.xml.transform.RewriteRule
import scala.xml.{Elem, Node}

trait HtmlServlet extends KoskiBaseServlet with AuthenticationSupport with HtmlNodes {
  lazy val buildVersion: Option[String] = Option(getServletContext.getResourceAsStream("/buildversion.txt")).map { i =>
    val p = new Properties()
    p.load(i)
    p.getProperty("vcsRevision", null)
  }

  override def haltWithStatus(status: HttpStatus): Nothing = status.statusCode match {
    case 401 => redirectToLogin
    case _ => super.haltWithStatus(status)
  }

  def renderStatus(status: HttpStatus): Unit = {
    val html = new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e: Elem =>
          if (e.label == "head") {
            e copy (child = e.child :+ htmlErrorObjectScript(status))
          } else {
            e copy (child = e.child flatMap transform)
          }
        case other => other
      }
    } transform htmlIndex("koski-main.js")

    response.setStatus(status.statusCode)
    contentType = "text/html"
    response.writer.print(html)
  }

  def renderObject(x: AnyRef) = x match {
    case e: Elem =>
      contentType = "text/html"
      response.writer.print(e.toString)
    case _ =>
      logger.error("HtmlServlet cannot render " + x)
      renderStatus(KoskiErrorCategory.internalError())
  }
}

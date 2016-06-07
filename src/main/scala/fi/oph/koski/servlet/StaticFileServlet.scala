package fi.oph.koski.servlet

import java.util.Properties

import fi.oph.koski.jettylauncher.JettyLauncher.staticResourcesRoot
import fi.oph.koski.util.Files
import org.scalatra.ScalatraServlet

class SingleFileServlet(val content: Content, matchedPaths: Seq[(String, Int)]) extends StaticFileServlet {
  matchedPaths.foreach { case (path, statusCode) =>
    get(path) {
      status = statusCode
      serveContent(content)
    }
  }
}

case class Content(contentType: String, text: String)

trait StaticFileServlet extends ScalatraServlet {
  def serveContent(content: Content) = {
    contentType = content.contentType
    content.text
  }

  def serveStaticFileIfExists(resourcePath: String) = StaticFileServlet.contentOf(resourcePath) match {
    case Some(content) =>
      serveContent(content)
    case None =>
      halt(404)
  }
}

object StaticFileServlet {
  lazy val indexHtml: Content = StaticFileServlet.contentOf(staticResourcesRoot + "/index.html").get

  private val properties: Properties = new Properties()
  properties.load(classOf[StaticFileServlet].getResourceAsStream("/mime.properties"))

  def resolveContentType(resourcePath: String) = {
    val extension = properties.get(suffix(resourcePath))
    if (extension != null) extension.toString() else "text/plain"
  }

  def contentOf(resourcePath: String) = {
    Files.asString(resourcePath).map(Content(resolveContentType(resourcePath), _))
  }

  private def suffix(path: String): String = path.reverse.takeWhile(_ != '.').reverse
}


package fi.oph.tor

import java.util.Properties

import fi.oph.tor.util.Files
import org.scalatra.ScalatraServlet

class SingleFileServlet(val content: Content, matchedPaths: Seq[(String, Int)]) extends StaticFileServlet {
  matchedPaths.foreach { case (path, statusCode) =>
    get(path) {
      status = statusCode
      serveContent(content)
    }
  }
}

class DirectoryServlet extends StaticFileServlet {
  get("/*") {
    val splatPath = multiParams("splat").head
    val resourcePath =
      splatPath.isEmpty match {
        case true  => request.getServletPath
        case false => request.getServletPath + "/" + splatPath
      }
    serveStaticFileIfExists(resourcePath)
  }
}

case class Content(contentType: String, bytes: Array[Byte])

trait StaticFileServlet extends ScalatraServlet {
  def serveContent(content: Content) = {
    contentType = content.contentType
    content.bytes
  }

  def serveStaticFileIfExists(resourcePath: String) = StaticFileServlet.contentOf(resourcePath) match {
    case Some(content) =>
      serveContent(content)
    case None =>
      halt(404)
  }
}

object StaticFileServlet {
  private val properties: Properties = new Properties()
  properties.load(classOf[StaticFileServlet].getResourceAsStream("/mime.properties"))

  def resolveContentType(resourcePath: String) = {
    val extension = properties.get(suffix(resourcePath))
    if (extension != null) extension.toString() else "text/plain"
  }

  def contentOf(resourcePath: String) = {
    Files.asByteArray(resourcePath).map(Content(resolveContentType(resourcePath), _))
  }

  private def suffix(path: String): String = path.reverse.takeWhile(_ != '.').reverse
}


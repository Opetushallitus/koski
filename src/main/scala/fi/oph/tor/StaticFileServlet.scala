package fi.oph.tor

import java.util.Properties

import fi.oph.tor.util.Files
import org.scalatra.ScalatraServlet

class SingleFileServlet(val resourcePath: String, matchedPaths: Seq[String], statusCode: Int = 200) extends StaticFileServlet {
  matchedPaths.foreach { path =>
    get(path) {
      status = 404
      serveStaticFileIfExists(resourcePath)
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

trait StaticFileServlet extends ScalatraServlet {
  def serveStaticFileIfExists(resourcePath: String) = Files.asByteArray(resourcePath) match {
    case Some(bytes) =>
      contentType = StaticFileServlet.resolveContentType(resourcePath)
      bytes
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

  private def suffix(path: String): String = path.reverse.takeWhile(_ != '.').reverse
}


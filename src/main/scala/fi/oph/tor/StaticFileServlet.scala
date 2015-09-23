package fi.oph.tor

import java.io.File
import java.util.Properties

import org.scalatra.ScalatraServlet
import scala.io.Source

class SingleFileServlet(val resourcePath: String) extends StaticFileServlet {

}

class MultiFileServlet extends StaticFileServlet {
  def resourcePath =
    splatPath.isEmpty match {
      case true  => request.getServletPath
      case false => request.getServletPath + "/" + splatPath
    }

  private def splatPath = multiParams("splat").head

}

trait StaticFileServlet extends ScalatraServlet {
  get("/*") {
    new File(resourcePath).exists() match {
      case true =>
        val staticFileContent = Source.fromFile(resourcePath).takeWhile(_ != -1).map(_.toByte).toArray
        contentType = StaticFileServlet.resolveContentType(resourcePath)
        println(contentType)
        staticFileContent
      case false =>
        halt(404)
    }
  }

  def resourcePath: String
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


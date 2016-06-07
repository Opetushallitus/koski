package fi.oph.koski.servlet

import fi.oph.koski.http.{KoskiErrorCategory, HttpStatus}
import fi.oph.koski.servlet.StaticFileServlet.indexHtml
import fi.oph.koski.koskiuser.AuthenticationSupport
import scala.xml.Elem

trait HtmlServlet extends AuthenticationSupport {
  def redirectToLogin = {
    redirect("/")
  }

  override def haltWithStatus(status: HttpStatus) = status.statusCode match {
    case 401 => redirectToLogin
    case _ => super.haltWithStatus(status)
  }

  def renderStatus(status: HttpStatus): Unit = {
    val errorInjectionScript = s"""|<script>
                                  |  window.koskiError = { httpStatus: ${status.statusCode}, text: "${status.errors(0).message.toString}", topLevel: true }
                                                                                                                                          |</script>""".stripMargin

    val bundleScriptTag: String = """<script id="bundle"""
    val indexHtmlWithInjectedScript: String = indexHtml.text.replace(bundleScriptTag, errorInjectionScript + bundleScriptTag)

    response.setStatus(status.statusCode)
    contentType = indexHtml.contentType
    response.writer.print(indexHtmlWithInjectedScript)
  }

  def renderObject(x: AnyRef) = x match {
    case e: Elem =>
      contentType = indexHtml.contentType
      response.writer.print(e.toString)
    case _ =>
      logger.error("HtmlServlet cannot render " + x)
      renderStatus(KoskiErrorCategory.internalError())
  }
}

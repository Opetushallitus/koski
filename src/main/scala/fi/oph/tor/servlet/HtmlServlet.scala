package fi.oph.tor.servlet

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.servlet.StaticFileServlet.indexHtml

trait HtmlServlet extends KoskiBaseServlet with StaticFileServlet {
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

  def renderObject(x: AnyRef) = {
    logger.error("HtmlServlet cannot render " + x)
    renderStatus(TorErrorCategory.internalError())
  }
}

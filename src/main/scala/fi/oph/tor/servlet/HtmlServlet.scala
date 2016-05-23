package fi.oph.tor.servlet

import fi.oph.tor.http.HttpStatus
import org.scalatra.ScalatraServlet

trait HtmlServlet extends ScalatraServlet with StaticFileServlet {
  def redirectToLogin = {
    redirect("/")
  }

  def renderStatus(status: HttpStatus) = {
    status.statusCode match {
      case 401 =>
        redirectToLogin
      case _ =>
        val indexHtml: Content = StaticFileServlet.contentOf("web/static/index.html").get

        val errorInjectionScript = s"""|<script>
                                       |  window.koskiError = { httpStatus: ${status.statusCode}, text: "${status.errors(0).message.toString}", topLevel: true }
                                       |</script>""".stripMargin

        halt(status = status.statusCode, body = serveContent(indexHtml.copy(text = indexHtml.text.replace("""<script id="pre-bundle"></script>""", errorInjectionScript))))
    }
  }
}

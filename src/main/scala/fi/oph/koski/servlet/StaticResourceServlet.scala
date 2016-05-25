package fi.oph.koski.servlet

import java.net.URL

import org.scalatra.ScalatraServlet

class StaticResourceServlet(path: String) extends ScalatraServlet {
  get("*") {
    val resource: Option[URL] = servletContext.resource("/")
    resource map { _ =>
      servletContext.getNamedDispatcher("default").forward(request, response)
    }
  }
}

package fi.oph.tor.schema

import org.scalatra.ScalatraServlet

class SchemaDocumentationServlet extends ScalatraServlet {
  get("/") {
    TorTiedonSiirtoHtml.html
  }
}

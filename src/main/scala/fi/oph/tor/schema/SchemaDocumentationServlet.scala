package fi.oph.tor.schema

import org.scalatra.ScalatraServlet

class SchemaDocumentationServlet extends ScalatraServlet {
  get("/") {
    TorTiedonSiirtoHtml.html
  }

  get("/tor-oppija-schema.json") {
    contentType = "application/json"
    TorSchema.schemaJsonString
  }

  get("/example.json") {
    contentType = "application/json"
    TorSchema.exampleJsonString
  }
}

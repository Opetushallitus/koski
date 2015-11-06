package fi.oph.tor.schema

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json

class SchemaDocumentationServlet extends ErrorHandlingServlet {
  get("/") {
    TorTiedonSiirtoHtml.html
  }

  get("/tor-oppija-schema.json") {
    contentType = "application/json"
    TorSchema.schemaJsonString
  }

  get("/examples/:name.json") {
    contentType = "application/json"

    TorOppijaExamples.examples.find(_.name == params("name")) match {
      case Some(example) => Json.write(example.oppija)
      case None => halt(404)
    }
  }
}

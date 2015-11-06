package fi.oph.tor.schema

import fi.oph.tor.json.Json
import org.scalatra.ScalatraServlet

class SchemaDocumentationServlet extends ScalatraServlet {
  get("/") {
    TorTiedonSiirtoHtml.html
  }

  get("/tor-oppija-schema.json") {
    contentType = "application/json"
    TorSchema.schemaJsonString
  }

  get("/:name.json") {
    contentType = "application/json"

    TorOppijaExamples.examples.find(_.name == params("name")) match {
      case Some(example) => Json.write(example.oppija)
      case None => halt(404)
    }
  }
}

package fi.oph.tor.schema

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}

class SchemaDocumentationServlet(koodistoPalvelu: KoodistoPalvelu) extends ErrorHandlingServlet {
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
      case Some(example) => Json.writePretty(example.data)
      case None => halt(404)
    }
  }

  get("/koodisto/:name/:version") {
    contentType = "application/json"
    val koodistoUri: String = params("name")
    val version = params("version") match {
      case "latest" =>
        koodistoPalvelu.getLatestVersion(koodistoUri)
      case x => x.toInt
    }
    Json.writePretty(koodistoPalvelu.getKoodistoKoodit(KoodistoViittaus(koodistoUri, version)))
  }
}

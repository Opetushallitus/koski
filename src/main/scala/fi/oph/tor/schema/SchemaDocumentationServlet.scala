package fi.oph.tor.schema

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.{KoodistoKoodi, KoodistoViite, LowLevelKoodistoPalvelu}

class SchemaDocumentationServlet(koodistoPalvelu: LowLevelKoodistoPalvelu) extends ErrorHandlingServlet {
  get("/") {
    TorTiedonSiirtoHtml.html
  }

  get("/tor-oppija-schema.json") {
    contentType = "application/json"
    TorSchema.schemaJsonString
  }

  get("/examples/:name.json") {
    contentType = "application/json"

    renderOption(TorOppijaExamples.examples.find(_.name == params("name")).map(_.data), pretty = true)
  }

  get("/koodisto/:name/:version") {
    contentType = "application/json"
    val koodistoUri: String = params("name")
    val versio = params("version") match {
      case "latest" =>
        koodistoPalvelu.getLatestVersion(koodistoUri)
      case x =>
        Some(KoodistoViite(koodistoUri, x.toInt))
    }
    val result: Option[List[KoodistoKoodi]] = versio.flatMap(koodistoPalvelu.getKoodistoKoodit)
    renderOption(result, pretty = true)
  }
}

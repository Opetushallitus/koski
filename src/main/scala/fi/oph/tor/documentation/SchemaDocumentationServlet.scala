package fi.oph.tor.documentation

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.koodisto.{KoodistoKoodi, KoodistoPalvelu, KoodistoViite}
import fi.oph.tor.schema.TorSchema
import fi.oph.tor.servlet.ErrorHandlingServlet

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

    renderOption(TorErrorCategory.notFound)(TorOppijaExamples.examples.find(_.name == params("name")).map(_.data), pretty = true)
  }

  get("/koodisto/:name/:version") {
    contentType = "application/json"
    val koodistoUri: String = params("name")
    val versio = params("version") match {
      case "latest" =>
        koodistoPalvelu.getLatestVersion(koodistoUri)
      case _ =>
        Some(KoodistoViite(koodistoUri, getIntegerParam("version")))
    }
    val result: Option[List[KoodistoKoodi]] = versio.flatMap(koodistoPalvelu.getKoodistoKoodit)
    renderOption(TorErrorCategory.notFound.koodistoaEiLöydy)(result, pretty = true)
  }
}

package fi.oph.koski.documentation

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.servlet.ApiServlet

class SchemaDocumentationServlet(koodistoPalvelu: KoodistoPalvelu) extends ApiServlet {
  get("/") {
    KoskiTiedonSiirtoHtml.html
  }

  get("/koski-oppija-schema.json") {
    KoskiSchema.schemaJson
  }

  get("/examples/:name.json") {
    renderOption(KoskiErrorCategory.notFound)(Examples.examples.find(_.name == params("name")).map(_.data))
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
    renderOption(KoskiErrorCategory.notFound.koodistoaEiLöydy)(result)
  }
}

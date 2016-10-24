package fi.oph.koski.documentation

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoPalvelu
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.servlet.ApiServlet

class SchemaDocumentationServlet(val koodistoPalvelu: KoodistoPalvelu) extends ApiServlet with Unauthenticated with KoodistoFinder {
  get("/") {
    KoskiTiedonSiirtoHtml.html
  }

  get("/koski-oppija-schema.json") {
    KoskiSchema.schemaJson
  }

  get("/examples/:name.json") {
    renderOption(KoskiErrorCategory.notFound)(Examples.allExamples.find(_.name == params("name")).map(_.data))
  }

  get("/koodisto/:name/:version") {
    contentType = "text/html"
    findKoodisto match {
      case Some((koodisto, koodit)) =>
        <html>
          <style>
            body {{ font-family: sans-serif; }}
            td, th {{ text-align: left; padding-right: 20px; }}
          </style>
          <body>
            <h1>Koodisto: { koodisto.koodistoUri }, versio { koodisto.versio } </h1>
            <table>
              <thead>
                <tr>
                  <th>Koodiarvo</th>
                  <th>Nimi</th>
                  <th>Lyhytnimi</th>
                </tr>
              </thead>
              <tbody>
                {
                koodit.sortBy(_.koodiArvo).map { koodi =>
                  <tr>
                    <td>{koodi.koodiArvo}</td>
                    <td>{koodi.metadata.find(_.kieli == Some("FI")).flatMap(_.nimi).getOrElse("&nbsp;")}</td>
                    <td>{koodi.metadata.find(_.kieli == Some("FI")).flatMap(_.lyhytNimi).getOrElse("&nbsp;")}</td>
                  </tr>
                }
                }
              </tbody>
            </table>
          </body>
        </html>
      case None => haltWithStatus(KoskiErrorCategory.notFound.koodistoaEiLöydy())
    }
  }
}

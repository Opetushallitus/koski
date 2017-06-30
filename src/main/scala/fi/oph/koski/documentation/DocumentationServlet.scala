package fi.oph.koski.documentation

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{KoodistoKoodiMetadata, KoodistoPalvelu}
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.{Henkilö, KoskiSchema, OsaamisenTunnustaminen}
import fi.oph.koski.servlet.ApiServlet
import fi.oph.scalaschema.ClassSchema

import scala.Function.const

class DocumentationServlet(val koodistoPalvelu: KoodistoPalvelu) extends ApiServlet with Unauthenticated with KoodistoFinder {
  get("/") {
    KoskiTiedonSiirtoHtml.html
  }

  get("/koski-oppija-schema.json") {
    KoskiSchema.schemaJson
  }

  get("/koski-oppija-schema.html") {
    def isHenkilöSchema(s: ClassSchema) = classOf[Henkilö].isAssignableFrom(Class.forName(s.fullClassName))
    params.get("entity") match {
      case None => KoskiSchemaDocumentHtml.html(
        focusEntities = isHenkilöSchema,
        shallowEntities = const(true)
      )
      case Some(focusEntityName) => KoskiSchemaDocumentHtml.html(
        focusEntities = { schema => schema.simpleName == focusEntityName || isHenkilöSchema(schema) },
        shallowEntities = { schema: ClassSchema => schema.fullClassName == classOf[OsaamisenTunnustaminen].getName }
      )
    }
  }

  get("/examples/:name.json") {
    renderOption(KoskiErrorCategory.notFound)(Examples.allExamples.find(_.name == params("name")).map(_.data))
  }

  get("/koodisto/:name/:version") {
    contentType = "text/html"
    findKoodisto match {
      case Some((koodisto, koodit)) =>
        <html>
          <head>
            <title>Koodisto: { koodisto.koodistoUri } - Koski - Opintopolku.fi</title>
          </head>
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
                  <th>Lyhytnimi</th>
                  <th>Nimi</th>
                  <th>Kuvaus</th>
                </tr>
              </thead>
              <tbody>
                {
                koodit.sortBy(_.koodiArvo).map { koodi =>
                  val metadata: Option[KoodistoKoodiMetadata] = koodi.metadata.find(_.kieli == Some(lang.toUpperCase))
                  <tr>
                    <td>{koodi.koodiArvo}</td>
                    <td>{metadata.flatMap(_.lyhytNimi).getOrElse("-")}</td>
                    <td>{metadata.flatMap(_.nimi).getOrElse("-")}</td>
                    <td>{metadata.flatMap(_.kuvaus).getOrElse("-")}</td>
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

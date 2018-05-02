package fi.oph.koski.documentation

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.html.{EiRaameja, Virkailija}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoKoodiMetadata
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.{Henkilö, OsaamisenTunnustaminen}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.scalaschema.ClassSchema
import org.scalatra.ScalatraServlet

import scala.Function.const

class DocumentationServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport with KoodistoFinder {
  val koodistoPalvelu = application.koodistoPalvelu

  get("/") {
    htmlIndex("koski-main.js", raamit = if (raamitHeaderSet && isAuthenticated) Virkailija else EiRaameja)
  }

  get("/koski-oppija-schema.html") {
    def isHenkilöSchema(s: ClassSchema) = classOf[Henkilö].isAssignableFrom(Class.forName(s.fullClassName))
    params.get("entity") match {
      case None => KoskiSchemaDocumentHtml.html(
        expandEntities = isHenkilöSchema,
        shallowEntities = const(true)
      )
      case Some(focusEntityName) => KoskiSchemaDocumentHtml.html(
        focusEntities = { schema => schema.simpleName == focusEntityName },
        expandEntities = isHenkilöSchema,
        shallowEntities = { schema: ClassSchema => schema.fullClassName == classOf[OsaamisenTunnustaminen].getName }
      )
    }
  }

  get("/koodisto/:name/:version") {
    contentType = "text/html"
    val kieli = Some(params.get("kieli").getOrElse(lang).toUpperCase)
    val kielet = LocalizedString.languages
    findKoodisto match {
      case Some((koodisto, koodit)) =>
        <html>
          <head>
            <title>Koodisto: { koodisto.koodistoUri } - Koski - Opintopolku.fi</title>
          </head>
          <style>
            body {{ font-family: sans-serif; }}
            td, th {{ text-align: left; padding-right: 20px; }}
            a {{ margin-right: 10px; }}
          </style>
          <body>
            <h1>Koodisto: { koodisto.koodistoUri }, versio { koodisto.versio } </h1>
            <p>{kielet.map { kieli => <a href={request.getRequestURI + "?kieli=" + kieli}>{kieli}</a> } }</p>
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
                  val metadata: Option[KoodistoKoodiMetadata] = koodi.metadata.find(_.kieli == kieli)
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

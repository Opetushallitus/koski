package fi.oph.koski.documentation

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.html.{EiRaameja, Raamit, Virkailija}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{Koodisto, KoodistoKoodiMetadata}
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.schema.{Henkilö, LocalizedString, OsaamisenTunnustaminen}
import fi.oph.koski.servlet.VirkailijaHtmlServlet
import fi.oph.scalaschema.ClassSchema
import org.scalatra.ScalatraServlet

import scala.Function.const
import scala.xml.NodeSeq

class DocumentationServlet(implicit val application: KoskiApplication)
  extends ScalatraServlet with VirkailijaHtmlServlet with KoskiSpecificAuthenticationSupport with KoodistoFinder {

  val koodistoPalvelu = application.koodistoPalvelu

  protected override def virkailijaRaamit: Raamit = if (virkailijaRaamitSet && isAuthenticated) Virkailija else EiRaameja

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("^/(|tietomalli|koodistot|rajapinnat/oppilashallintojarjestelmat|rajapinnat/luovutuspalvelu|rajapinnat/palveluvayla-omadata|rajapinnat/massaluovutus/koulutuksenjarjestajat)$".r)(nonce => {
    htmlIndex("koski-main.js", raamit = virkailijaRaamit, allowIndexing = true, nonce = nonce)
  })

  get("/koski-oppija-schema.html")(nonce => {
    def isHenkilöSchema(s: ClassSchema) = classOf[Henkilö].isAssignableFrom(Class.forName(s.fullClassName))

    params.get("entity") match {
      case None => KoskiSchemaDocumentHtml.html(
        expandEntities = isHenkilöSchema,
        shallowEntities = const(true),
        lang = lang,
        nonce = nonce
      )
      case Some(focusEntityName) => KoskiSchemaDocumentHtml.html(
        focusEntities = { schema => schema.simpleName == focusEntityName },
        expandEntities = isHenkilöSchema,
        shallowEntities = { schema: ClassSchema => schema.fullClassName == classOf[OsaamisenTunnustaminen].getName },
        lang = lang,
        nonce = nonce
      )
    }
  })

  get("/koodisto/:name/:version")(nonce => {
    contentType = "text/html"
    val kieli = Some(params.get("kieli").getOrElse(lang).toUpperCase)
    val kielet = LocalizedString.languages

    findKoodisto match {
      case Some((koodisto, koodit)) =>
        <html lang={lang}>
          <head>
            <title>Koodisto: { koodisto.koodistoUri } - Koski - Opintopolku.fi</title>
          </head>
          <style nonce={nonce}>
            body {{ font-family: sans-serif; }}
            td, th {{ text-align: left; padding-right: 20px; }}
            a {{ margin-right: 10px; }}
          </style>
          <body>
            <h1>Koodisto: { koodisto.koodistoUri }, versio { koodisto.versio } </h1>
            <p>{koodistonKuvausJaLinkki(koodisto, kieli.get)}</p>
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
  })

  private def koodistonKuvausJaLinkki(koodisto: Koodisto, kieli: String): NodeSeq = {
    val KuvausLinkillä = "^(.*)(https?://[^ )]+)(.*)$".r
    koodisto.kuvaus.map(_.get(kieli)) match {
      case None => NodeSeq.Empty
      case Some(k) => k match {
        case KuvausLinkillä(prefix, link, suffix) => <p>{prefix}<a href={link} rel="nofollow">{link}</a>{suffix}</p>
        case _ => <p>{k}</p>
      }
    }
  }
}

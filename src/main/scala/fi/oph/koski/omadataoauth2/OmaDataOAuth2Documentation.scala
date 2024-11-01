package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.Markdown
import fi.oph.koski.log.Logging
import fi.oph.koski.util.TryWithLogging

import java.net.URL
import scala.io.Source

// TODO: TOR-2210: Tähän lisää dokumentaatiota, esimerkkejä yms., jonka generointiin voi ottaa lisää mallia massaluovutuksen QueryDocumentation-luokasta
object OmaDataOAuth2Documentation extends Logging {
  // Skeema-jsonit

  // HTML-stringit, jotka palautetaan polusta /koski/api/documentation/sections.html
  private val sectionSources = Map(
    "oauth2_omadata" -> "documentation/omadata_oauth2.md"
  )

  def htmlTextSections(application: KoskiApplication): Map[String, String] =
    sectionSources.mapValues(htmlTextSection(application))

  def htmlTextSection(application: KoskiApplication)(path: String): String =
    TryWithLogging.andResources(logger, { use =>
      val source = use(Source.fromResource(path)).mkString
      val html = Markdown.markdownToXhtmlString(source)
      addVariableTexts(application, html)
    }).getOrElse(missingSection(path))

  def missingSection(name: String): String =
    <p style="color: red"><b>Virhe:</b> resurssia {name} ei löydy</p>
      .toString()

  def addVariableTexts(application: KoskiApplication, markdown: String): String = {
    val rootUrl = new URL(application.config.getString("koski.root.url"))
    val baseUrl = rootUrl.toString

    val luovutuspalveluRootUrl = application.config.getString("omadataoauth2.luovutuspalveluBaseUrl") match {
      case "mock" => new URL("https://localhost:7022")
      case url => new URL(url)
    }
    val luovutuspalveluBaseUrl = luovutuspalveluRootUrl.toString

    val vars = Map(
      "baseUrl" -> baseUrl,
      "luovutuspalveluBaseUrl" -> luovutuspalveluBaseUrl
    )

    logger.info(vars.toString)

    "\\{\\{var:(.+?)\\}\\}"
      .r("name")
      .replaceAllIn(markdown, { m => vars.getOrElse(m.group("name"), "!!! NOT FOUND !!!") })
  }
}

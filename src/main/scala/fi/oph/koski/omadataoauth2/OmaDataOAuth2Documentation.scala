package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.Markdown
import fi.oph.koski.log.Logging
import fi.oph.koski.util.TryWithLogging
import fi.oph.koski.xml.NodeSeqImplicits._

import java.net.URL
import scala.io.Source

object OmaDataOAuth2Documentation extends Logging {
  // HTML-stringit, jotka palautetaan polusta /koski/api/documentation/sections.html
  private val sectionSources = Map(
    "oauth2_omadata" -> "documentation/omadata_oauth2.md"
  )

  def htmlTextSections(application: KoskiApplication): Map[String, String] =
    sectionSources.view.mapValues(htmlTextSection(application)).toMap

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
    val virkailijaBaseUrl = new URL(
      application.config.getString("opintopolku.virkailija.url") match {
        case "mock" => "http://localhost:7021/koski"
        case url => s"${url}/koski"
    }).toString

    val oppijaBaseUrl = new URL(
      application.config.getString("opintopolku.oppija.url") match {
        case "mock" => "http://localhost:7021/koski"
        case url => s"${url}/koski"
      }).toString

    val luovutuspalveluRootUrl = application.config.getString("omadataoauth2.luovutuspalveluBaseUrl") match {
      case "mock" => new URL("https://localhost:7022")
      case url => new URL(url)
    }
    val luovutuspalveluBaseUrl = luovutuspalveluRootUrl.toString

    val vars = Map(
      "virkailijaBaseUrl" -> virkailijaBaseUrl,
      "oppijaBaseUrl" -> oppijaBaseUrl,
      "luovutuspalveluBaseUrl" -> luovutuspalveluBaseUrl
    )

    "\\{\\{var:(.+?)\\}\\}"
      .r("name")
      .replaceAllIn(markdown, { m => vars.getOrElse(m.group("name"), "!!! NOT FOUND !!!") })
  }
}

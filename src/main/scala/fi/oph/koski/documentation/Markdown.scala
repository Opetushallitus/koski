package fi.oph.koski.documentation

import fi.oph.koski.log.Logging
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import scala.xml.{Node, Text, Unparsed}

object Markdown extends Logging {
  private val parser = Parser.builder().build()
  private val renderer = HtmlRenderer.builder().build()

  def markdownToXhtml(markdown: String): Node = try {
    val document = parser.parse(markdown)
    Unparsed(renderer.render(document))
  } catch {
    case e: Exception =>
      logger.error(e)(s"Error rendering markdown")
      Text(markdown)
  }

  def markdownToXhtmlString(markdown: String): String = markdownToXhtml(markdown).toString
}

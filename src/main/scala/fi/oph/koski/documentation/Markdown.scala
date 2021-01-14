package fi.oph.koski.documentation

import com.tristanhunt.knockoff.DefaultDiscounter.{knockoff, toXHTML}
import fi.oph.common.log.Logging

import scala.xml.Node
object Markdown extends Logging {
  def markdownToXhtml(markdown: String): Node = try {
    toXHTML(knockoff(markdown))
  } catch {
    case e: Exception =>
      logger.error(e)(s"Error rendering $markdown as markdown")
      <span>markdown</span>
  }
  def markdownToXhtmlString(markdown: String): String = markdownToXhtml(markdown).toString
}

package fi.oph.koski.localization

import fi.oph.koski.schema.annotation.{Deprecated, InfoDescription, InfoLinkTitle, InfoLinkUrl, Tooltip}
import fi.oph.scalaschema.{ClassSchema, Property}
import fi.oph.scalaschema.annotation.Description

object KoskiSpecificSchemaLocalization {
  type KeyAndText = (String, String)

  def title(property: Property): KeyAndText = (property.title, property.title)
  def description(property: Property): List[KeyAndText] = property.metadata.collect({ case Description(d) => shortKeyAndText("description:", d) })
  def infoDescription(property: Property): List[KeyAndText] = property.metadata.collect({ case InfoDescription(d) => shortKeyAndText("infoDescription:", d) })
  def infoLinkTitle(property: Property): List[KeyAndText] = property.metadata.collect({ case InfoLinkTitle(d) => shortKeyAndText("infoLinkTitle:", d) })
  def infoLinkUrl(property: Property): List[KeyAndText] = property.metadata.collect({ case InfoLinkUrl(d) => shortKeyAndText("infoLinkUrl:", d) })
  def tooltip(property: Property): List[KeyAndText] = property.metadata.collect({ case Tooltip(d) => shortKeyAndText("tooltip:", d) })
  def deprecated(property: Property): Option[KeyAndText] = property.metadata.collectFirst { case Deprecated(d) => shortKeyAndText("deprecated:", d) }

  def description(schema: ClassSchema): List[KeyAndText] = schema.metadata.collect({ case Description(d) => shortKeyAndText("description:", d) })
  def allLocalizableParts(schema: ClassSchema) = description(schema) ++ schema.properties.flatMap { p => title(p) :: description(p) ++ infoLinkTitle(p) ++ infoLinkUrl(p) ++ infoDescription(p) ++ tooltip(p) ++ deprecated(p) }

  private def shortKeyAndText(prefix: String, text: String): KeyAndText = {
    val key = text.split(" ") match {
      case parts if parts.length > 5 =>
        parts.take(5).mkString(" ") + "..."
      case _ => text
    }
    (prefix + key, text)
  }
}

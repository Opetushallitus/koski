package fi.oph.koski.localization

import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.{ClassSchema, Property, SchemaJsonDecorator}
import org.json4s.JsonAST.{JObject, JString, JValue}

class SchemaLocalizationEnricher(localizations: Map[String, LocalizedString]) extends SchemaJsonDecorator {
  import SchemaLocalizationEnricher.KeyAndText

  override def decorateProperty(property: Property, json: JObject): JObject = {
    val translated = translationFor(
      List(KoskiSpecificSchemaLocalization.title(property)),
      KoskiSpecificSchemaLocalization.description(property)
    ).fold(json)(withTranslation(json, _))
    deprecatedTextFor(property).fold(translated)(text =>
      JObject(translated.obj :+ ("deprecatedText", JString(text): JValue)))
  }

  override def decorateClass(schema: ClassSchema, json: JObject): JObject =
    translationFor(List((schema.title, schema.title)), KoskiSpecificSchemaLocalization.description(schema))
      .fold(json)(withTranslation(json, _))

  private def withTranslation(node: JObject, translation: JObject): JObject =
    JObject(node.obj :+ ("translation", translation: JValue))

  private def deprecatedTextFor(property: Property): Option[String] =
    KoskiSpecificSchemaLocalization.deprecated(property).flatMap {
      case (key, _) => localizations.get(key).flatMap(_.getOptional("fi"))
    }

  def translationFor(titleParts: List[KeyAndText], descriptionParts: List[KeyAndText]): Option[JObject] = {
    val byLanguage = SchemaLocalizationEnricher.displayedLanguages.flatMap { lang =>
      val fields = List(
        "title" -> textFor(titleParts, lang),
        "description" -> textFor(descriptionParts, lang)
      ).collect { case (key, Some(text)) => key -> (JString(text): JValue) }
      if (fields.isEmpty) None else Some(lang -> (JObject(fields: _*): JValue))
    }
    if (byLanguage.isEmpty) None else Some(JObject(byLanguage: _*))
  }

  private def textFor(parts: List[KeyAndText], lang: String): Option[String] =
    parts.flatMap { case (key, _) => localizations.get(key).flatMap(_.getOptional(lang)) }.headOption
}

object SchemaLocalizationEnricher {
  type KeyAndText = (String, String)

  val displayedLanguages: List[String] = List("fi", "sv", "en")
}

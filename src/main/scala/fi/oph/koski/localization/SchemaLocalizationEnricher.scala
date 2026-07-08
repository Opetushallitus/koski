package fi.oph.koski.localization

import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.ClassSchema
import org.json4s.JsonAST.{JObject, JString, JValue}

class SchemaLocalizationEnricher(localizations: Map[String, LocalizedString]) {
  import SchemaLocalizationEnricher.KeyAndText

  def enrich(schema: ClassSchema, json: JObject): JObject = {
    val definitionsBySimpleName = schema.definitions.collect { case c: ClassSchema => c.simpleName -> c }.toMap
    val withDefinitions = JObject(json.obj.map {
      case ("definitions", JObject(defs)) =>
        "definitions" -> JObject(defs.map {
          case (name, defJson: JObject) =>
            name -> definitionsBySimpleName.get(name).fold(defJson: JValue)(injectClassBody(_, defJson))
          case other => other
        })
      case other => other
    })
    injectClassBody(schema, withDefinitions)
  }

  private def injectClassBody(schema: ClassSchema, classJson: JObject): JObject = {
    val propertiesByKey = schema.properties.map(p => p.key -> p).toMap
    val withProperties = JObject(classJson.obj.map {
      case ("properties", JObject(props)) =>
        "properties" -> JObject(props.map {
          case (key, node: JObject) =>
            val translation = propertiesByKey.get(key)
              .flatMap(p => translationFor(List(KoskiSpecificSchemaLocalization.title(p)), KoskiSpecificSchemaLocalization.description(p)))
            key -> translation.fold(node: JValue)(withTranslation(node, _))
          case other => other
        })
      case other => other
    })
    val classTitle = List((schema.title, schema.title))
    translationFor(classTitle, KoskiSpecificSchemaLocalization.description(schema))
      .fold(withProperties)(withTranslation(withProperties, _))
  }

  private def withTranslation(node: JObject, translation: JObject): JObject =
    JObject(node.obj :+ ("translation", translation: JValue))

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

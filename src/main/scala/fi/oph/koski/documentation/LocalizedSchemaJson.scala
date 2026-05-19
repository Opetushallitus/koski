package fi.oph.koski.documentation

import java.util.concurrent.ConcurrentHashMap

import fi.oph.koski.localization.KoskiSpecificSchemaLocalization
import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.{ClassSchema, Property}
import org.json4s._

// Walks the SchemaToJson-emitted JSON tree alongside the typed Schema, attaching
// `translation: { fi: [...], sv: [...], en: [...] }` per node based on the
// localization keys produced by KoskiSpecificSchemaLocalization. Most translatable
// metadata (@Tooltip, @InfoDescription, ...) never reaches the JSON, so the typed
// schema is required to discover the keys.
//
// Results are cached per schema name and invalidated whenever the localization
// snapshot returns a new map reference.
class LocalizedSchemaJson(localizationsSnapshot: () => Map[String, LocalizedString]) {

  private val cache = new ConcurrentHashMap[String, (AnyRef, JValue)]()

  def translated(schemaName: String, schemaJson: => JValue, schema: => ClassSchema): JValue = {
    val current = localizationsSnapshot()
    val currentRef = current.asInstanceOf[AnyRef]
    val cached = cache.get(schemaName)
    if (cached != null && (cached._1 eq currentRef)) {
      cached._2
    } else {
      val built = annotate(schemaJson, schema, current)
      cache.put(schemaName, (currentRef, built))
      built
    }
  }

  private def annotate(rawJson: JValue, root: ClassSchema, lookup: Map[String, LocalizedString]): JValue = {
    val byName: Map[String, ClassSchema] =
      root.definitions.collect { case cs: ClassSchema => cs.simpleName -> cs }.toMap +
        (root.simpleName -> root)

    rawJson match {
      case rootObj: JObject =>
        val annotatedRoot = annotateClassDefinition(rootObj, root, lookup)
        updateField(annotatedRoot, "definitions") {
          case JObject(defs) =>
            JObject(defs.map { case (name, defJson) =>
              (defJson, byName.get(name)) match {
                case (obj: JObject, Some(cs)) => name -> annotateClassDefinition(obj, cs, lookup)
                case _ => name -> defJson
              }
            })
          case other => other
        }
      case other => other
    }
  }

  private def annotateClassDefinition(classJson: JObject, classSchema: ClassSchema, lookup: Map[String, LocalizedString]): JObject = {
    val classKeys = KoskiSpecificSchemaLocalization.description(classSchema).map(_._1)
    val withClassTrans = mergeTranslation(classJson, translationField(classKeys, lookup))
    updateField(withClassTrans, "properties") {
      case JObject(propEntries) =>
        JObject(propEntries.map { case (key, propJson) =>
          (propJson, classSchema.properties.find(_.key == key)) match {
            case (obj: JObject, Some(prop)) => key -> annotateProperty(obj, prop, lookup)
            case _ => key -> propJson
          }
        })
      case other => other
    }
  }

  private def annotateProperty(propJson: JObject, property: Property, lookup: Map[String, LocalizedString]): JObject = {
    import KoskiSpecificSchemaLocalization._
    val keys: List[String] =
      title(property)._1 ::
        description(property).map(_._1) :::
        tooltip(property).map(_._1) :::
        infoDescription(property).map(_._1) :::
        infoLinkTitle(property).map(_._1) :::
        infoLinkUrl(property).map(_._1) :::
        deprecated(property).toList.map(_._1)
    mergeTranslation(propJson, translationField(keys, lookup))
  }

  private def translationField(keys: List[String], lookup: Map[String, LocalizedString]): Option[JObject] = {
    val byLocale = LocalizedString.languages.flatMap { lang =>
      val values = keys.flatMap(k => lookup.get(k).flatMap(_.getOptional(lang))).filter(_.nonEmpty)
      if (values.isEmpty) None else Some(lang -> values)
    }
    if (byLocale.isEmpty) None
    else Some(JObject(byLocale.map { case (lang, values) =>
      lang -> JArray(values.map(JString(_)))
    }))
  }

  private def mergeTranslation(obj: JObject, translation: Option[JObject]): JObject = translation match {
    case Some(t) => obj.merge(JObject("translation" -> t))
    case None => obj
  }

  private def updateField(obj: JObject, name: String)(f: JValue => JValue): JObject =
    JObject(obj.obj.map { case (k, v) => if (k == name) (k, f(v)) else (k, v) })
}

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
    val classDescriptions = KoskiSpecificSchemaLocalization.description(classSchema)
    val (withDescription, overriddenKeys) = overrideDescription(classJson, classDescriptions, lookup)
    val entries = asEntries(classDescriptions, withFiFallback = true)
    val withClassTrans = mergeTranslation(withDescription, translationField(entries, lookup, overriddenKeys))
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
    val descriptions = description(property)
    val entries: List[(String, Option[String])] =
      (title(property)._1, None) ::
        asEntries(descriptions, withFiFallback = true) :::
        tooltip(property).map(t => (t._1, None)) :::
        infoDescription(property).map(t => (t._1, None)) :::
        infoLinkTitle(property).map(t => (t._1, None)) :::
        infoLinkUrl(property).map(t => (t._1, None)) :::
        deprecated(property).toList.map(t => (t._1, None))
    val (withDescription, overriddenKeys) = overrideDescription(propJson, descriptions, lookup)
    mergeTranslation(withDescription, translationField(entries, lookup, overriddenKeys))
  }

  private def asEntries(keysAndText: List[(String, String)], withFiFallback: Boolean): List[(String, Option[String])] =
    keysAndText.map { case (k, t) => (k, if (withFiFallback) Some(t) else None) }

  // Substitutes each @Description's text with its Tolgee fi value inside the JSON
  // description field. Substring replacement (not whole-field) so that annotation-
  // appended suffixes like "(Oksa: …)", "(Koodisto: …)", "(Vanhentunut kenttä: …)"
  // survive — those come from other metadata via appendToDescription. Returns the
  // set of keys whose fi value was applied so the caller can drop them from
  // translation.fi (no point shipping the same value twice).
  private def overrideDescription(obj: JObject, descriptions: List[(String, String)], lookup: Map[String, LocalizedString]): (JObject, Set[String]) = {
    val currentDesc = (obj \ "description") match {
      case JString(s) => s
      case _ => ""
    }
    if (currentDesc.isEmpty) (obj, Set.empty)
    else {
      val (updated, used) = descriptions.foldLeft((currentDesc, Set.empty[String])) {
        case ((acc, usedAcc), (key, originalText)) =>
          lookup.get(key).flatMap(_.getOptional("fi")).filter(_.nonEmpty) match {
            case Some(fi) if acc.contains(originalText) => (acc.replace(originalText, fi), usedAcc + key)
            case _ => (acc, usedAcc)
          }
      }
      val out = if (updated == currentDesc) obj else obj.merge(JObject("description" -> JString(updated)))
      (out, used)
    }
  }

  // Builds the per-locale translation field. Each entry is a Tolgee lookup key
  // plus an optional fi fallback (the code @Description text). The fallback
  // only kicks in when Tolgee has no entry at all for that key — if Tolgee has
  // the key, its values (including missing fi) are trusted as-is. Keys in
  // `excludeFromFi` contribute nothing to translation.fi (used to dedup when
  // the description JSON field already shows the Tolgee fi).
  private def translationField(
    entries: List[(String, Option[String])],
    lookup: Map[String, LocalizedString],
    excludeFromFi: Set[String] = Set.empty
  ): Option[JObject] = {
    val byLocale = LocalizedString.languages.flatMap { lang =>
      val values = entries.flatMap { case (key, fiFallback) =>
        if (lang == "fi" && excludeFromFi.contains(key)) None
        else lookup.get(key) match {
          case Some(localized) => localized.getOptional(lang).filter(_.nonEmpty)
          case None if lang == "fi" => fiFallback.filter(_.nonEmpty)
          case None => None
        }
      }
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

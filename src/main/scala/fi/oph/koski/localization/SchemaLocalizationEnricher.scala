package fi.oph.koski.localization

import fi.oph.koski.schema.LocalizedString
import org.json4s.JsonAST.{JArray, JObject, JString}

class SchemaLocalizationEnricher(localizations: Map[String, LocalizedString]) {
  import SchemaLocalizationEnricher.KeyAndText

  def translationFor(titleParts: List[KeyAndText], descriptionParts: List[KeyAndText]): Option[JObject] = {
    val entries = List(
      "Otsikko" -> lines(titleParts),
      "Kuvaus" -> lines(descriptionParts)
    ).collect { case (label, ls) if ls.nonEmpty => label -> JArray(ls.map(JString)) }
    if (entries.isEmpty) None else Some(JObject(entries: _*))
  }

  private def lines(parts: List[KeyAndText]): List[String] =
    parts.flatMap { case (key, _) =>
      localizations.get(key).toList.flatMap { localized =>
        LocalizedString.languages.flatMap(lang => localized.getOptional(lang).map(text => s"$lang: $text"))
      }
    }
}

object SchemaLocalizationEnricher {
  type KeyAndText = (String, String)
}

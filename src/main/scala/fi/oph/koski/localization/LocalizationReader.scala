package fi.oph.koski.localization

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

class LocalizationReader(repository: LocalizationRepository, val language: String) {
  def get(key: String): String = repository.get(key).get(language)

  def get(key: String, params: Map[String, String]): String = params.foldLeft(get(key))((acc, kv) => acc.replaceAll("\\{\\{" + kv._1 + "\\}\\}", kv._2))

  def from(str: LocalizedString): String = str.get(language)

  def fromKoodiviite(orElse: String)(koodi: Koodistokoodiviite): String = koodi.nimi.map(from).getOrElse(get(orElse))

  def format(date: LocalDate): String = date.format(finnishDateFormat)

  def formatDateString(date: String): String = format(LocalDate.parse(date))

  def pick(fiText: String, svText: String): String = if (language == "sv") svText else fiText

  def pick(fiText: Option[String], svText: Option[String], fallback: String): String =
    (if (language == "sv") List(svText, fiText) else List(fiText, svText))
      .flatten
      .headOption
      .getOrElse(fallback)
}

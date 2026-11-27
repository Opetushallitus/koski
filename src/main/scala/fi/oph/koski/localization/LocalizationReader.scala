package fi.oph.koski.localization

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate
import scala.collection.concurrent.TrieMap

class LocalizationReader(
  repository: LocalizationRepository,
  val language: String,
  koodistoViitePalvelu: Option[KoodistoViitePalvelu] = None
) {
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

  private val ruotsinkielisetNimetKoodistoittain = TrieMap.empty[String, Map[String, String]]

  // Osa raporttien koodiarvoista on suomenkielisiä sanoja (esim. opiskeluoikeuden tila "lasna"). Ruotsiksi näytetään
  // koodin ruotsinkielinen nimi, jos se poikkeaa suomenkielisestä. Muuten, ja muilla kielillä, näytetään koodiarvo.
  def koodiarvo(koodistoUri: String, koodiarvo: String): String =
    koodistoViitePalvelu
      .filter(_ => language == "sv")
      .flatMap(palvelu => ruotsinkielisetNimetKoodistoittain.getOrElseUpdate(koodistoUri, ruotsinkielisetNimet(palvelu, koodistoUri)).get(koodiarvo))
      .getOrElse(koodiarvo)

  private def ruotsinkielisetNimet(palvelu: KoodistoViitePalvelu, koodistoUri: String): Map[String, String] =
    palvelu
      .getKoodistoKoodiViitteet(palvelu.getLatestVersionRequired(koodistoUri))
      .flatMap { koodi =>
        val nimet = koodi.nimi.map(_.values).getOrElse(Map.empty[String, String])
        nimet.get("sv").filterNot(nimet.get("fi").contains).map(koodi.koodiarvo -> _)
      }
      .toMap
}

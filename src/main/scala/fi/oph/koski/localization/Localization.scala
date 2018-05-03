package fi.oph.koski.localization

import fi.oph.koski.localization.LocalizedString.missingString
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.annotation.Representative
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.{Formats, JObject, JValue, Serializer}
import org.json4s.reflect.TypeInfo

@Description("Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan")
trait LocalizedString extends Localized {
  def valueList: List[(String, String)]
  lazy val values: Map[String, String] = Map(valueList : _*)
  def get(lang: String) = values.get(lang).orElse(values.get("fi")).orElse(values.values.headOption).getOrElse(missingString)
  def concat(x: LocalizedString) = {
    val (fi :: sv :: en :: Nil) = LocalizedString.languages.map { lang => get(lang) + x.get(lang) }
    Finnish(fi, Some(sv), Some(en))
  }
  def description = this
  def hasLanguage(lang: String): Boolean = valueList.map(_._1).contains(lang)
}

trait Localizable {
  def description(texts: LocalizationRepository): LocalizedString
}

trait Localized extends Localizable {
  def description: LocalizedString
  def description(texts: LocalizationRepository): LocalizedString = description
}

@Description("Lokalisoitu teksti, jossa mukana suomi")
@Title("Suomeksi")
case class Finnish(@Representative fi: String, sv: Option[String] = None, en: Option[String] = None) extends LocalizedString {
  def valueList = (("fi" -> fi) :: sv.toList.map(("sv", _))) ++ en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana ruotsi")
@Title("Ruotsiksi")
case class Swedish(@Representative sv: String, en: Option[String] = None) extends LocalizedString {
  def valueList = ("sv" -> sv) :: en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana englanti")
@Title("Englanniksi")
case class English(@Representative en: String) extends LocalizedString {
  def valueList = List(("en" -> en))
}


object LocalizedString extends Logging {
  val missingString = "???"
  val languages = List("fi", "sv", "en")
  /**
   * Sanitize map of localized values:
   * 1. lowercase all keys
   * 2. ditch empty values
   * 3. return None if no non-empty values available
   * 4. patch it to always contain a Finnish value
   */
  def sanitize(values: Map[String, String]): Option[LocalizedString] = {
    def getAny(values: Map[String, String]) = {
      //logger.warn("Finnish localization missing from " + values)
      values.toList.map(_._2).headOption.getOrElse(missingString)
    }
    val lowerCased = values.flatMap {
      case (key, "") => None
      case (key, value) => Some((key.toLowerCase(), value))
    }.toMap

    lowerCased.isEmpty match {
      case true => None
      case false => Some(Finnish(lowerCased.getOrElse("fi", getAny(lowerCased)), lowerCased.get("sv"), lowerCased.get("en")))
    }
  }

  def sanitizeRequired(values: Map[String, String], defaultValue: String): LocalizedString = sanitize(values).getOrElse(unlocalized(defaultValue))

  val empty: LocalizedString = Finnish("", Some(""), Some(""))
  val missing: LocalizedString = unlocalized(missingString)
  def unlocalized(string: String): LocalizedString = Finnish(string, Some(string), Some(string))
  def finnish(string: String): LocalizedString = Finnish(string)
  def swedish(string: String): LocalizedString = Swedish(string)
  def english(string: String): LocalizedString = English(string)
  def concat(strings: LocalizedString*) = strings.foldLeft(LocalizedString.empty) {
    case (built, next) => built.concat(next)
  }
}

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)
  implicit def localized2localizedString(localized: Localized): LocalizedString = localized.description
  implicit object LocalizedStringFinnishOrdering extends Ordering[LocalizedString] {
    override def compare(x: LocalizedString, y: LocalizedString) = x.get("fi").compareTo(y.get("fi"))
  }
}

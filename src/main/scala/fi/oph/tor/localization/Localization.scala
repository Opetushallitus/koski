package fi.oph.tor.localization

import fi.oph.tor.localization.LocalizedString.missingString
import fi.oph.tor.log.Logging
import fi.oph.scalaschema.annotation.Description

@Description("Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan")
trait LocalizedString extends Localizable {
  def valueList: List[(String, String)]
  lazy val values: Map[String, String] = Map(valueList : _*)
  def get(lang: String) = values.get(lang).orElse(values.get("fi")).orElse(values.values.headOption).getOrElse(missingString)
  def description = this
  def concat(x: LocalizedString) = {
    val (fi :: sv :: en :: Nil) = LocalizedString.languages.map { lang => get(lang) + x.get(lang) }
    Finnish(fi, Some(sv), Some(en))
  }
}

trait Localizable {
  def description: LocalizedString
}

@Description("Lokalisoitu teksti, jossa mukana suomi")
case class Finnish(fi: String, sv: Option[String] = None, en: Option[String] = None) extends LocalizedString {
  def valueList = (("fi" -> fi) :: sv.toList.map(("sv", _))) ++ en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana ruotsi")
case class Swedish(sv: String, fi: Option[String] = None, en: Option[String] = None) extends LocalizedString {
  def valueList = (("sv" -> sv) :: fi.toList.map(("fi", _))) ++ en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana englanti")
case class English(en: String, sv: Option[String] = None, fi: Option[String] = None) extends LocalizedString {
  def valueList = (("en" -> en) :: sv.toList.map(("sv", _))) ++ fi.toList.map(("fi", _))
}


object LocalizedString extends Logging {
  val missingString = "???"
  def languages = List("fi", "sv", "en")
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

  def sanitizeRequired(values: Map[String, String], defaultValue: String): LocalizedString = sanitize(values).getOrElse(missing)

  val empty: LocalizedString = Finnish("", Some(""), Some(""))
  val missing: LocalizedString = unlocalized(missingString)
  def unlocalized(string: String): LocalizedString = Finnish(string, Some(string), Some(string))
  def finnish(string: String): LocalizedString = Finnish(string)
  def swedish(string: String): LocalizedString = Swedish(string)
  def english(string: String): LocalizedString = English(string)
  def concat(strings: Any*) = strings.foldLeft(LocalizedString.empty) {
    case (built, next) => built.concat(fromAny(next))
  }
  def fromAny(thing: Any) = thing match {
    case x: Localizable => x.description
    case x: Any => unlocalized(x.toString)
  }
}

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)
}
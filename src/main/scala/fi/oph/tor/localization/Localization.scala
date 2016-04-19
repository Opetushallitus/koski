package fi.oph.tor.localization

import fi.oph.tor.log.Logging
import fi.oph.tor.schema.generic.annotation.Description

@Description("Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan")
trait LocalizedString {
  def valueList: List[(String, String)]
  lazy val values: Map[String, String] = Map(valueList : _*)
  def get(lang: String) = values.get(lang).orElse(values.get("fi")).getOrElse("")
  override def toString = get("fi") // TODO: remove this, should not be used in UI

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
  /**
   * Sanitize map of localized values:
   * 1. lowercase all keys
   * 2. ditch empty values
   * 3. return None if no non-empty values available
   * 4. patch it to always contain a Finnish value
   */
  def apply(values: Map[String, String]): Option[LocalizedString] = {
    def getAny(values: Map[String, String]) = {
      //logger.warn("Finnish localization missing from " + values)
      values.toList.map(_._2).headOption.getOrElse("???")
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
  val missing: LocalizedString = finnish("???")
  def finnish(string: String): LocalizedString = Finnish(string)
  def swedish(string: String): LocalizedString = Swedish(string)
  def english(string: String): LocalizedString = English(string)
}

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)
}
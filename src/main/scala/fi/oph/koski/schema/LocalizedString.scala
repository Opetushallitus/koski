package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.Representative
import fi.oph.scalaschema.annotation.{Description, Title}

trait BlankableLocalizedString {
  def valueList: List[(String, String)]
  lazy val values: Map[String, String] = Map(valueList : _*)
  def get(lang: String) = values.get(lang).orElse(values.get("fi")).orElse(values.values.headOption).getOrElse(LocalizedString.missingString)
  /**
   * Returns the translation, if it exists in the values-map. If the translation cannot be found for the given language, no fallbacks are searched and None is returned.
   */
  def getOptional(lang: String) = values.get(lang)
  def concat(x: LocalizedString) = {
    val (fi :: sv :: en :: Nil) = LocalizedString.languages.map { lang => get(lang) + x.get(lang) }
    Finnish(fi, Some(sv), Some(en))
  }
  def hasLanguage(lang: String): Boolean = valueList.map(_._1).contains(lang)
  def toLocalizedString: Option[LocalizedString]
}

// Placeholder-implementaatio tyhjälle tekstille scala-schemaa varten.
// Mahdollistaa BlankableLocalizedString-tyyppisen elementin, jossa lokalisoitu
// teksti on tyhjä kaikilla kielillä.
@Description("Lokalisoitu teksti, joka on tyhjä kaikilla kielillä")
case class BlankLocalizedString() extends BlankableLocalizedString {
  override def valueList: List[(String, String)] = List()

  override def toLocalizedString: Option[LocalizedString] = None
}

object LocalizedString {
  val missingString = "???"
  val languages = List("fi", "sv", "en")
  /**
   * Sanitize map of localized values:
   * 1. lowercase all keys
   * 2. ditch empty values
   * 3. return None if no non-empty values available
   * 4. patch it to always contain a Finnish value
   */
  def sanitize(values: Map[String, String]): Option[Finnish] = {
    def getAny(values: Map[String, String]) = {
      //logger.warn("Finnish localization missing from " + values)
      values.toList.map(_._2).headOption.getOrElse(missingString)
    }
    val lowerCased = values.flatMap {
      case (key, "") => None
      case (key, value) => Some((key.toLowerCase(), value))
    }

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

trait Localized {
  def description: LocalizedString
}

@Description("Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan")
trait LocalizedString extends BlankableLocalizedString with Localized {
  override def description: LocalizedString = this

  override def toLocalizedString: Option[LocalizedString] = Some(this)
}

@Description("Lokalisoitu teksti, jossa mukana suomi")
@Title("Suomeksi")
case class Finnish(@Representative fi: String, sv: Option[String] = None, en: Option[String] = None) extends LocalizedString {
  override def valueList = (("fi" -> fi) :: sv.toList.map(("sv", _))) ++ en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana ruotsi")
@Title("Ruotsiksi")
case class Swedish(@Representative sv: String, en: Option[String] = None) extends LocalizedString {
  override def valueList = ("sv" -> sv) :: en.toList.map(("en", _))
}

@Description("Lokalisoitu teksti, jossa mukana englanti")
@Title("Englanniksi")
case class English(@Representative en: String) extends LocalizedString {
  override def valueList = List(("en" -> en))
}




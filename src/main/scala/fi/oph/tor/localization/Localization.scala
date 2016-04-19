package fi.oph.tor.localization

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.{ContextualExtractor, Json}
import fi.oph.tor.log.Logging
import fi.oph.tor.schema._
import fi.oph.tor.schema.generic.annotation.Description
import org.json4s._
import org.json4s.reflect.TypeInfo

@Description("Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan")
case class LocalizedString(fi: Option[String], sv: Option[String] = None, en: Option[String] = None) {
  lazy val values: Map[String, String] = {
    val values: List[(String, String)] = ((fi.toList.map(("fi", _))) ++ sv.toList.map(("sv", _))) ++ en.toList.map(("en", _))
    Map(values : _*)
  }
  def get(lang: String) = values.get(lang).orElse(values.get("fi")).getOrElse("")

  override def toString = get("fi") // TODO: remove this, should not be used in UI
}

object LocalizedString extends Logging {
  def apply(values: Map[String, String]): LocalizedString = {
    def getAny(values: Map[String, String]) = {
      //logger.warn("Finnish localization missing from " + values)
      values.toList.map(_._2).headOption.getOrElse("???")
    }
    val lowerCased = values.map { case (key, value) => (key.toLowerCase(), value) }.toMap
    LocalizedString(Some(lowerCased.getOrElse("fi", getAny(lowerCased))), lowerCased.get("sv"), lowerCased.get("en"))
  }
  def finnish(string: String): LocalizedString = LocalizedString(fi = Some(string))
  def swedish(string: String): LocalizedString = LocalizedString(fi = None, sv = Some(string))
  def english(string: String): LocalizedString = LocalizedString(fi = None, en = Some(string))
}

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)
}

object LocalizedStringValidatingDeserializer extends Deserializer[LocalizedString] {
  val LocalizedStringClass = classOf[LocalizedString]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LocalizedString] = {
    case (TypeInfo(LocalizedStringClass, _), json) =>
      val deserialized = json.extract[LocalizedString](Json.jsonFormats, Manifest.classType(LocalizedStringClass))
      deserialized.values.isEmpty match {
        case true => ContextualExtractor.extractionError(TorErrorCategory.badRequest.validation.localizedTextMissing())
        case false => deserialized
      }
  }
}
package fi.oph.tor.localization

import fi.oph.tor.log.Logging

case class LocalizedString(fi: String, sv: Option[String] = None, en: Option[String] = None) {
  lazy val values: Map[String, String] = Map(((("fi" -> fi) :: sv.toList.map(("sv", _))) ++ en.toList.map(("en", _))) :_*)
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
    LocalizedString(lowerCased.getOrElse("fi", getAny(lowerCased)), lowerCased.get("sv"), lowerCased.get("en"))
  }
  implicit def unlocalized(string: String): LocalizedString = LocalizedString(string)
}
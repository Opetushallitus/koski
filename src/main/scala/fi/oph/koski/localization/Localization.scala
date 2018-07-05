package fi.oph.koski.localization

import fi.oph.koski.schema.LocalizedString.missingString
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Localized, LocalizedString}

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)
  implicit def localized2localizedString(localized: Localized): LocalizedString = localized.description
  implicit object LocalizedStringFinnishOrdering extends Ordering[LocalizedString] {
    override def compare(x: LocalizedString, y: LocalizedString) = x.get("fi").compareTo(y.get("fi"))
  }
}

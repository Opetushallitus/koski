package fi.oph.koski.localization

import fi.oph.koski.schema.{Localized, LocalizedString}

import scala.math.Ordering.OptionOrdering
import scala.language.implicitConversions

object LocalizedStringImplicits {
  implicit def str2localized(string: String): LocalizedString = LocalizedString.finnish(string)

  lazy val localizedStringFinnishOrdering: Ordering[LocalizedString] = new Ordering[LocalizedString] {
    override def compare(x: LocalizedString, y: LocalizedString) = x.get("fi").compareTo(y.get("fi"))
  }
  lazy val localizedStringOptionFinnishOrdering = new OptionOrdering[LocalizedString] { val optionOrdering = localizedStringFinnishOrdering }
}



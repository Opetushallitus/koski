package fi.oph.koski.raportointikanta

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.{KoodiViite, Koodistokoodiviite}

object LoaderUtils {
  def convertLocalizedString(s: Option[LocalizedString]): String =
    s.map(_.get("fi")).getOrElse(LocalizedString.missingString)

  def convertKoodisto(k: KoodiViite): Option[String] = k match {
    case kkv: Koodistokoodiviite => Some(kkv.koodistoUri)
    case _ => None
  }
}

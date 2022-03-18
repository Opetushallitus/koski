package fi.oph.koski.raportointikanta

import fi.oph.koski.schema.{KoodiViite, Koodistokoodiviite, LocalizedString}

object LoaderUtils {
  def convertLocalizedString(s: Option[LocalizedString], lang: String): String = {
    s.map(_.get(lang)).getOrElse(LocalizedString.missingString)
  }

  def convertKoodisto(k: KoodiViite): Option[String] = k match {
    case kkv: Koodistokoodiviite => Some(kkv.koodistoUri)
    case _ => None
  }
}

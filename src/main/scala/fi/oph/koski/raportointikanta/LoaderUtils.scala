package fi.oph.koski.raportointikanta

import fi.oph.koski.localization.LocalizedString

object LoaderUtils {
  def convertLocalizedString(s: Option[LocalizedString]): String =
    s.map(_.get("fi")).getOrElse(LocalizedString.missingString)
}

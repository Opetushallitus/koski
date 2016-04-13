package fi.oph.tor.tutkinto

import fi.oph.tor.localization.LocalizedString

case class TutkintoPeruste(diaarinumero: String, tutkintoKoodi: String, nimi: Option[LocalizedString], rakenne: Option[TutkintoRakenne] = None)

package fi.oph.koski.tutkinto

import fi.oph.common.schema.LocalizedString

case class TutkintoPeruste(diaarinumero: String, tutkintoKoodi: String, nimi: Option[LocalizedString], rakenne: Option[TutkintoRakenne] = None)

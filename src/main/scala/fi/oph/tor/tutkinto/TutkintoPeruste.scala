package fi.oph.tor.tutkinto

case class TutkintoPeruste(diaarinumero: String, tutkintoKoodi: String, nimi: Option[String], rakenne: Option[TutkintoRakenne] = None)

package fi.oph.tor.tutkinto

case class TutkintoPeruste(ePerusteetDiaarinumero: String, tutkintoKoodi: String, nimi: Option[String], rakenne: Option[TutkintoRakenne] = None)

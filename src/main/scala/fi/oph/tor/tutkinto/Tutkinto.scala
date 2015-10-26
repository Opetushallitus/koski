package fi.oph.tor.tutkinto

case class Tutkinto(ePerusteetDiaarinumero: String, tutkintoKoodi: String, nimi: Option[String], rakenne: Option[TutkintoRakenne] = None)

package fi.oph.tor.tutkinto

case class TutkintoRakenne(suoritustavat: List[Suoritustapa], osaamisalat: List[Osaamisala])

case class Suoritustapa(nimi: String, koodi: String, rakenne: RakenneOsa)
case class Osaamisala(nimi: String, koodi: String)

sealed trait RakenneOsa
case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(nimi: String) extends RakenneOsa

package fi.oph.tor.tutkinto

sealed trait RakenneOsa

case class TutkintoRakenne(suoritustavat: List[Suoritustapa], osaamisalat: List[Osaamisala])
case class Suoritustapa(nimi: String, koodi: String, rakenne: RakenneOsa)
case class Osaamisala(nimi: String, koodi: String)
case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(nimi: String) extends RakenneOsa

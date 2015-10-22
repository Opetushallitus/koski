package fi.oph.tor.tutkinto

sealed trait RakenneOsa

case class RakenneModuuli(nimi: String, osat: List[RakenneOsa]) extends RakenneOsa
case class TutkinnonOsa(nimi: String) extends RakenneOsa

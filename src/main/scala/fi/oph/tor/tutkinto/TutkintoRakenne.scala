package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
import fi.oph.tor.koodisto.KoodistoViittaus

case class TutkintoRakenne(suoritustavat: List[Suoritustapa], osaamisalat: List[Osaamisala], arviointiAsteikot: List[Arviointiasteikko])

case class Suoritustapa(nimi: String, koodi: String, rakenne: RakenneOsa)
case class Osaamisala(nimi: String, koodi: String)

sealed trait RakenneOsa
case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(tunniste: KoulutusModuuliTunniste, nimi: String, arviointiAsteikko: KoodistoViittaus) extends RakenneOsa
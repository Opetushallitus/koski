package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
import fi.oph.tor.eperusteet.ESuoritustapa
import fi.oph.tor.koodisto.KoodistoViittaus

case class TutkintoRakenne(suoritustavat: List[SuoritustapaJaRakenne], osaamisalat: List[Osaamisala], arviointiAsteikot: List[Arviointiasteikko]) {
}

object TutkintoRakenne {
  def findTutkinnonOsa(rakenne: TutkintoRakenne, koulutusModuuliTunniste: KoulutusModuuliTunniste): Option[TutkinnonOsa] = rakenne.suoritustavat.flatMap(suoritustapa => findTutkinnonOsa(suoritustapa.rakenne, koulutusModuuliTunniste)).headOption

  def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: KoulutusModuuliTunniste): Option[TutkinnonOsa] = rakenne match {
    case t:TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t:RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }
  def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodi == osaamisAlaKoodi)


}

case class Suoritustapa(nimi: String, koodi: String)

object Suoritustapa {
  val ops = Suoritustapa("OPS", "ops")
  val naytto = Suoritustapa("Näyttö", "naytto")

  val suoritustavat = List(ops, naytto)

  def apply(koodi: String): Option[Suoritustapa] = suoritustavat.find(_.koodi == koodi)
  def apply(suoritustapa: ESuoritustapa): Suoritustapa = apply(suoritustapa.suoritustapakoodi).getOrElse(throw new IllegalArgumentException("Suoritustapa " + suoritustapa))
}

case class SuoritustapaJaRakenne(suoritustapa: Suoritustapa, rakenne: RakenneOsa)


case class Osaamisala(nimi: String, koodi: String)

sealed trait RakenneOsa

case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(tunniste: KoulutusModuuliTunniste, nimi: String, arviointiAsteikko: Option[KoodistoViittaus]) extends RakenneOsa
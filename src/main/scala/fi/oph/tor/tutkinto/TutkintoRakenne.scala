package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
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
  def apply(koodi: String): Option[Suoritustapa] = koodi match { // TODO: i18n
    case "ops" => Some(Suoritustapa("OPS", koodi))
    case "naytto" => Some(Suoritustapa("Näyttö", koodi))
    case _ => None
  }
}

case class SuoritustapaJaRakenne(suoritustapa: Suoritustapa, rakenne: RakenneOsa)


case class Osaamisala(nimi: String, koodi: String)

sealed trait RakenneOsa

case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(tunniste: KoulutusModuuliTunniste, nimi: String, arviointiAsteikko: KoodistoViittaus) extends RakenneOsa
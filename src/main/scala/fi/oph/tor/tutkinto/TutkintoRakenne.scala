package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
import fi.oph.tor.eperusteet.ESuoritustapa
import fi.oph.tor.koodisto.KoodistoViittaus
import fi.oph.tor.schema.KoodistoKoodiViite

case class TutkintoRakenne(suoritustavat: List[SuoritustapaJaRakenne], osaamisalat: List[Osaamisala], arviointiAsteikot: List[Arviointiasteikko]) {
}

object TutkintoRakenne {
  def findTutkinnonOsa(rakenne: TutkintoRakenne, suoritustapa: KoodistoKoodiViite, koulutusModuuliTunniste: KoulutusModuuliTunniste): Option[TutkinnonOsa] = {
    rakenne.suoritustavat.find(_.suoritustapa == suoritustapa).flatMap(suoritustapa => findTutkinnonOsa(suoritustapa.rakenne, koulutusModuuliTunniste)).headOption
  }

  def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: KoulutusModuuliTunniste): Option[TutkinnonOsa] = rakenne match {
    case t:TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t:RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }
  def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodi == osaamisAlaKoodi)
}

case class SuoritustapaJaRakenne(suoritustapa: KoodistoKoodiViite, rakenne: RakenneOsa)

case class Osaamisala(nimi: String, koodi: String)

sealed trait RakenneOsa

case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(tunniste: KoulutusModuuliTunniste, nimi: String, arviointiAsteikko: Option[KoodistoViittaus]) extends RakenneOsa
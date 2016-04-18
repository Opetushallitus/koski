package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
import fi.oph.tor.koodisto.KoodistoViite
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.schema.{Suoritustapa, Koodistokoodiviite}
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

case class TutkintoRakenne(diaarinumero: String, koulutustyyppi: Koulutustyyppi, suoritustavat: List[SuoritustapaJaRakenne], osaamisalat: List[Koodistokoodiviite], arviointiAsteikot: List[Arviointiasteikko]) {
  def findSuoritustapaJaRakenne(suoritustapa: Suoritustapa): Option[SuoritustapaJaRakenne] = {
    suoritustavat.find(_.suoritustapa == suoritustapa.tunniste)
  }
}

case class SuoritustapaJaRakenne(suoritustapa: Koodistokoodiviite, rakenne: Option[RakenneOsa], laajuusYksikkö: Option[Koodistokoodiviite])

sealed trait RakenneOsa

case class RakenneModuuli(nimi: LocalizedString, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa {
  def tutkinnonOsat: List[TutkinnonOsa] = osat flatMap {
    case m: RakenneModuuli => m.tutkinnonOsat
    case o: TutkinnonOsa => List(o)
  }
}
case class TutkinnonOsa(tunniste: Koodistokoodiviite, nimi: LocalizedString, arviointiAsteikko: Option[KoodistoViite], laajuus: Option[Float], pakollinen: Boolean) extends RakenneOsa
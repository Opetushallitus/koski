package fi.oph.koski.tutkinto

import fi.oph.koski.arvosana.Arviointiasteikko
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.{AmmatillisenTutkinnonSuoritustapa, Koodistokoodiviite}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

case class TutkintoRakenne(diaarinumero: String, koulutustyyppi: Koulutustyyppi, suoritustavat: List[SuoritustapaJaRakenne], osaamisalat: List[Koodistokoodiviite], arviointiAsteikot: List[Arviointiasteikko]) {
  def findSuoritustapaJaRakenne(suoritustapa: AmmatillisenTutkinnonSuoritustapa): Option[SuoritustapaJaRakenne] = {
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
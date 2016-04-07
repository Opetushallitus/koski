package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.Arviointiasteikko
import fi.oph.tor.koodisto.KoodistoViite
import fi.oph.tor.schema.Koodistokoodiviite

case class TutkintoRakenne(diaarinumero: String, suoritustavat: List[SuoritustapaJaRakenne], osaamisalat: List[Osaamisala], arviointiAsteikot: List[Arviointiasteikko])

case class SuoritustapaJaRakenne(suoritustapa: Koodistokoodiviite, rakenne: RakenneOsa, laajuusYksikkö: Option[Koodistokoodiviite])

case class Osaamisala(nimi: String, koodiarvo: String)

sealed trait RakenneOsa

case class RakenneModuuli(nimi: String, osat: List[RakenneOsa], osaamisalaKoodi: Option[String]) extends RakenneOsa
case class TutkinnonOsa(tunniste: Koodistokoodiviite, nimi: String, arviointiAsteikko: Option[KoodistoViite], laajuus: Option[Float], pakollinen: Boolean) extends RakenneOsa
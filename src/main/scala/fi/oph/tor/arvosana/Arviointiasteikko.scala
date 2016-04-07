package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.KoodistoViite
import fi.oph.tor.schema.Koodistokoodiviite

case class Arviointiasteikko(koodisto: KoodistoViite, arvosanat: List[Koodistokoodiviite])

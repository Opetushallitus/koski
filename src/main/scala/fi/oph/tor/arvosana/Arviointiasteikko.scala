package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.KoodistoViittaus
import fi.oph.tor.schema.KoodistoKoodiViite

case class Arviointiasteikko(koodisto: KoodistoViittaus, arvosanat: List[KoodistoKoodiViite]) {
}

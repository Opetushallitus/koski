package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.KoodistoViittaus

case class Arviointiasteikko(koodisto: KoodistoViittaus, arvosanat: List[Arvosana]) {
}

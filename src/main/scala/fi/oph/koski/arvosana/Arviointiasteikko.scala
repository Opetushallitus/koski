package fi.oph.koski.arvosana

import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.schema.Koodistokoodiviite

case class Arviointiasteikko(koodisto: KoodistoViite, arvosanat: List[Koodistokoodiviite])

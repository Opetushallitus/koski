package fi.oph.koski.koodisto

import fi.oph.koski.log.Loggable

case class KoodistoViite(koodistoUri: String, versio: Int) extends Loggable {
  def logString = koodistoUri + "/" + versio
}


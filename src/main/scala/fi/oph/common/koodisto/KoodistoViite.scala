package fi.oph.common.koodisto

import fi.oph.common.log.Loggable

case class KoodistoViite(koodistoUri: String, versio: Int) extends Loggable {
  def logString = koodistoUri + "/" + versio
}

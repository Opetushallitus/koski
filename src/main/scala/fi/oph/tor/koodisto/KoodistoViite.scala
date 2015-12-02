package fi.oph.tor.koodisto

case class KoodistoViite(koodistoUri: String, versio: Int) {
  override def toString = koodistoUri + "/" + versio
}


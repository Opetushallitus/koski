package fi.oph.tor.koodisto

case class KoodistoViittaus(koodistoUri: String, versio: Int) {
  override def toString = koodistoUri + "/" + versio
}


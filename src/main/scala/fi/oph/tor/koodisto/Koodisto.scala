package fi.oph.tor.koodisto

case class Koodisto(koodistoUri: String, versio: Int, metadata: List[KoodistoMetadata])

case class KoodistoMetadata(kieli: String, kasite: Option[String])
package fi.oph.tor.koodisto

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int) {
  def nimi(kieli: String) = metadata.find(_.kieli == Some(kieli.toUpperCase)).flatMap(_.nimi)
}
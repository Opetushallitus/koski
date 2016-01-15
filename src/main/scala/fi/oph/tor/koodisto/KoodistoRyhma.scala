package fi.oph.tor.koodisto

case class KoodistoRyhmä(koodistoRyhmaMetadatas: List[KoodistoRyhmäMetadata]) {
  def this(nimi: String) = this(List("FI", "SV", "EN").map(lang => KoodistoRyhmäMetadata(nimi, lang)))
}

case class KoodistoRyhmäMetadata(nimi: String, kieli: String = "FI")

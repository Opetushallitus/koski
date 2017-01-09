package fi.oph.koski.koodisto

case class KoodistoRyhmä(koodistoRyhmaMetadatas: List[KoodistoRyhmäMetadata])

object KoodistoRyhmä {
  def apply(nimi: String): KoodistoRyhmä = KoodistoRyhmä(List("FI", "SV", "EN").map(lang => KoodistoRyhmäMetadata(nimi, lang)))
}

case class KoodistoRyhmäMetadata(nimi: String, kieli: String = "FI")

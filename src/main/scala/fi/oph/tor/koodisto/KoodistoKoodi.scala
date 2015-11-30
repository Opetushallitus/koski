package fi.oph.tor.koodisto

import java.time.LocalDate

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int, voimassaAlkuPvm: Option[LocalDate]) {
  def nimi(kieli: String) = metadata.find(_.kieli == Some(kieli.toUpperCase)).flatMap(_.nimi)
}
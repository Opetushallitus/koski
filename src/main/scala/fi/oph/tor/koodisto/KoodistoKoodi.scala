package fi.oph.tor.koodisto

import java.time.LocalDate

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int, voimassaAlkuPvm: Option[LocalDate]) {
  def nimi(kieli: String) = getMetadata(kieli).flatMap(_.nimi)
  def lyhytNimi(kieli: String) = getMetadata(kieli).flatMap(_.lyhytNimi.filter(_.length > 0))

  def getMetadata(kieli: String): Option[KoodistoKoodiMetadata] = {
    metadata.find(_.kieli == Some(kieli.toUpperCase))
  }

}
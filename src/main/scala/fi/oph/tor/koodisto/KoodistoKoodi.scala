package fi.oph.tor.koodisto

import java.time.LocalDate

import fi.oph.tor.localization.LocalizedString

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int, voimassaAlkuPvm: Option[LocalDate]) {
  def nimi: LocalizedString = {
    val values: Map[String, String] = metadata.flatMap { meta => meta.nimi.flatMap { nimi => meta.kieli.map { kieli => (kieli, nimi) } } }.toMap
    LocalizedString(values)
  }

  def lyhytNimi(kieli: String) = getMetadata(kieli).flatMap(_.lyhytNimi.filter(_.length > 0))

  def getMetadata(kieli: String): Option[KoodistoKoodiMetadata] = {
    metadata.find(_.kieli == Some(kieli.toUpperCase))
  }

}
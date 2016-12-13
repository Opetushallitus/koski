package fi.oph.koski.koodisto

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int, version: Option[Long], voimassaAlkuPvm: Option[LocalDate], tila: Option[String] = None) {
  private def localizedStringFromMetadata(f: KoodistoKoodiMetadata => Option[String]): Option[LocalizedString] = {
    val values: Map[String, String] = metadata.flatMap { meta => f(meta).flatMap { nimi => meta.kieli.map { kieli => (kieli, nimi) } } }.toMap
    LocalizedString.sanitize(values)
  }

  def nimi = localizedStringFromMetadata { meta => meta.nimi.map(_.trim) }

  def lyhytNimi = localizedStringFromMetadata { meta => meta.lyhytNimi.map(_.trim) }

  def getMetadata(kieli: String): Option[KoodistoKoodiMetadata] = {
    metadata.find(_.kieli == Some(kieli.toUpperCase))
  }

}
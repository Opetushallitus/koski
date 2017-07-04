package fi.oph.koski.koodisto

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString

case class KoodistoKoodi(koodiUri: String, koodiArvo: String, metadata: List[KoodistoKoodiMetadata], versio: Int, version: Option[Long], voimassaAlkuPvm: Option[LocalDate], tila: Option[String] = None, withinCodeElements: List[CodeRelationship] = Nil) {
  private def localizedStringFromMetadata(f: KoodistoKoodiMetadata => Option[String]): Option[LocalizedString] = {
    val values: Map[String, String] = metadata.flatMap { meta => f(meta).flatMap { nimi => meta.kieli.map { kieli => (kieli, nimi) } } }.toMap
    LocalizedString.sanitize(values)
  }

  def nimi = localizedStringFromMetadata { meta => meta.nimi.map(_.trim) }

  def lyhytNimi = localizedStringFromMetadata { meta => meta.lyhytNimi.map(_.trim) }

  def getMetadata(kieli: String): Option[KoodistoKoodiMetadata] = {
    metadata.find(_.kieli == Some(kieli.toUpperCase))
  }

  def withAdditionalInfo(additionalInfo: CodeAdditionalInfo) = copy(metadata = additionalInfo.metadata, withinCodeElements = additionalInfo.withinCodeElements)
}

object KoodistoKoodi {
  def koodiUri(koodistoUri: String, koodiarvo: String) = {
    koodistoUri + "_" + koodiarvo.toLowerCase.replaceAll("\\.|/", "").replaceAll("ä", "a").replaceAll("ö", "o")
  }
}

case class KoodistoKoodiMetadata(nimi: Option[String], lyhytNimi: Option[String] = None, kuvaus: Option[String] = None, kieli: Option[String])
case class CodeAdditionalInfo(metadata: List[KoodistoKoodiMetadata], withinCodeElements: List[CodeRelationship]) // The info that can only be gotten by GETting the individual code from koodisto-service
case class CodeRelationship(codeElementUri: String, codeElementVersion: Int, passive: Boolean = false)
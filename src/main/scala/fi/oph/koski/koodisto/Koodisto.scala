package fi.oph.koski.koodisto

import java.time.LocalDate

case class Koodisto(koodistoUri: String, versio: Int, metadata: List[KoodistoMetadata], codesGroupUri: String, voimassaAlkuPvm: LocalDate, organisaatioOid: String) {
  def koodistoViite = KoodistoViite(koodistoUri, versio)
}

case class KoodistoMetadata(kieli: String, nimi: Option[String], kuvaus: Option[String], kasite: Option[String])
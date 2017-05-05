package fi.oph.koski.koodisto

import java.time.LocalDate

// Tätä oliota käytetään myös koodistojen luonnissa. Älä poista kenttiä!
case class Koodisto(koodistoUri: String, versio: Int, metadata: List[KoodistoMetadata], codesGroupUri: String, voimassaAlkuPvm: LocalDate, organisaatioOid: String, withinCodes: List[KoodistoRelationship] = Nil, tila: String = "LUONNOS", version: Int = 0) {
  def koodistoViite = KoodistoViite(koodistoUri, versio)
}

case class KoodistoMetadata(kieli: String, nimi: Option[String], kuvaus: Option[String])

case class KoodistoRelationship(codesUri: String, codesVersion: Int, passive: Boolean = false)
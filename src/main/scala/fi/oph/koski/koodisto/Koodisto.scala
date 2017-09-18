package fi.oph.koski.koodisto

import java.time.LocalDate

import fi.oph.scalaschema.annotation.DefaultValue

// Tätä oliota käytetään myös koodistojen luonnissa. Älä poista kenttiä!
case class Koodisto(koodistoUri: String, versio: Int, metadata: List[KoodistoMetadata], codesGroupUri: String, voimassaAlkuPvm: LocalDate, organisaatioOid: String, withinCodes: Option[List[KoodistoRelationship]] = None, @DefaultValue("LUONNOS") tila: String = "LUONNOS", @DefaultValue(0) version: Int = 0) {
  def koodistoViite = KoodistoViite(koodistoUri, versio)
}

case class KoodistoMetadata(kieli: String, nimi: Option[String], kuvaus: Option[String])

case class KoodistoRelationship(codesUri: String, codesVersion: Int, @DefaultValue(false) passive: Boolean = false)
package fi.oph.koski.koodisto

import java.time.LocalDate

import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.annotation.DefaultValue

// Tätä oliota käytetään myös koodistojen luonnissa. Älä poista kenttiä!
case class Koodisto(
  koodistoUri: String,
  versio: Int,
  metadata: List[KoodistoMetadata],
  codesGroupUri: String,
  voimassaAlkuPvm: LocalDate,
  organisaatioOid: String,
  withinCodes: Option[List[KoodistoRelationship]] = None,
  @DefaultValue("LUONNOS") tila: String = "LUONNOS",
  @DefaultValue(0) version: Int = 0
) {
  def koodistoViite = KoodistoViite(koodistoUri, versio)
  def nimi = localizedStringFromMetadata { meta => meta.nimi.map(_.trim) }
  def kuvaus = localizedStringFromMetadata { meta => meta.kuvaus.map(_.trim) }

  private def localizedStringFromMetadata(f: KoodistoMetadata => Option[String]): Option[LocalizedString] = {
    val values: Map[String, String] = metadata.flatMap { meta => f(meta).map { nimi => (meta.kieli, nimi) } }.toMap
    LocalizedString.sanitize(values)
  }

}

case class KoodistoMetadata(kieli: String, nimi: Option[String], kuvaus: Option[String])

case class KoodistoRelationship(codesUri: String, codesVersion: Int, @DefaultValue(false) passive: Boolean = false)

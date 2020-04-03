package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.{Description, SyntheticProperty}

trait Koulusivistyskieli {
  @SyntheticProperty
  @Description("Koulusivistyskieli. Tiedon syötössä tietoa ei tarvita; tieto poimitaan osasuorituksista.")
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")
  def koulusivistyskieli: Option[List[Koodistokoodiviite]]
}

object Koulusivistyskieli {
  val suomi = Some(Koodistokoodiviite("FI", Some(Finnish("suomi")), "kieli"))
  val ruotsi = Some(Koodistokoodiviite("SV", Some(Finnish("ruotsi")), "kieli"))
}

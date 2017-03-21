package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.scalaschema.annotation.{Description, MinValueExclusive}

trait Koulutusmoduuli extends Localizable {
  @Representative
  @Discriminator
  def tunniste: KoodiViite
  def laajuus: Option[Laajuus]
  def nimi: LocalizedString
  def description: LocalizedString = nimi
  def isTutkinto = false
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait Koulutus extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Tutkinnon 6-numeroinen tutkintokoodi")
  @KoodistoUri("koulutus")
  @OksaUri("tmpOKSAID560", "tutkinto")
  def tunniste: Koodistokoodiviite
}

trait DiaarinumerollinenKoulutus extends Koulutus {
  @Description("Tutkinnon perusteen diaarinumero Ks. ePerusteet-palvelu")
  def perusteenDiaarinumero: Option[String]
}

trait PaikallinenKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: PaikallinenKoodi
  def nimi = tunniste.nimi
}

@Description("Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä")
trait Laajuus {
  @Description("Opintojen laajuuden arvo")
  @MinValueExclusive(0)
  def arvo: Float
  @Description("Opintojen laajuuden yksikkö")
  @KoodistoUri("opintojenlaajuusyksikko")
  def yksikkö: Koodistokoodiviite
}

trait Valinnaisuus {
  @Description("Onko pakollinen osa tutkinnossa (true/false)")
  def pakollinen: Boolean
}
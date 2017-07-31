package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.{Localizable, LocalizationRepository, LocalizedString}
import fi.oph.scalaschema.annotation.{Description, Discriminator, MinValueExclusive, Title}

trait Koulutusmoduuli extends Localizable {
  @Representative
  @Discriminator
  @ReadOnly("Tunnistetta ei voi muuttaa")
  def tunniste: KoodiViite
  def laajuus: Option[Laajuus]
  def nimi: LocalizedString
  def description(texts: LocalizationRepository): LocalizedString = nimi
  def isTutkinto = false
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait Koulutus extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Tutkinnon 6-numeroinen tutkintokoodi. Sama kuin tilastokeskuksen koulutuskoodi.")
  @KoodistoUri("koulutus")
  @OksaUri("tmpOKSAID560", "tutkinto")
  def tunniste: Koodistokoodiviite
}

trait Diaarinumerollinen {
  @Description("Tutkinnon perusteen diaarinumero Ks. ePerusteet-palvelu")
  @Title("Peruste")
  @ClassName("peruste")
  def perusteenDiaarinumero: Option[String]
}

trait DiaarinumerollinenKoulutus extends Koulutus with Diaarinumerollinen

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

trait Kieliaine extends Koulutusmoduuli {
  def kieli: Koodistokoodiviite
}

trait Äidinkieli extends Kieliaine
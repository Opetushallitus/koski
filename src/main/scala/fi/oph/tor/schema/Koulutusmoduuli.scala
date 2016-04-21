package fi.oph.tor.schema

import fi.oph.tor.localization.LocalizedString._
import fi.oph.tor.localization.{Localizable, LocalizedString}
import fi.oph.tor.schema.generic.annotation.{MinValue, Description}

trait Koulutusmoduuli extends Localizable {
  def tunniste: KoodiViite
  def laajuus: Option[Laajuus] = None
  def nimi: LocalizedString
  def description: LocalizedString = nimi
}

trait KoodistostaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Koodistokoodiviite
  def nimi: LocalizedString = tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo))
}

trait EPerusteistaLöytyväKoulutusmoduuli extends Koulutusmoduuli {
  @Description("Tutkinnon perusteen diaarinumero Ks. ePerusteet-palvelu")
  def perusteenDiaarinumero: Option[String]
}

trait PaikallinenKoulutusmoduuli extends Koulutusmoduuli {
  def tunniste: Paikallinenkoodi
  def nimi = tunniste.nimi
}

@Description("Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä")
case class Laajuus(
  @Description("Opintojen laajuuden arvo")
  @MinValue(0)
  arvo: Float,
  @Description("Opintojen laajuuden yksikkö")
  @KoodistoUri("opintojenlaajuusyksikko")
  yksikkö: Koodistokoodiviite
)
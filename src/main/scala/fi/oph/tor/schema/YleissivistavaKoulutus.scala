package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description
import fi.oph.tor.localization.LocalizedStringImplicits._

trait Oppiaineensuoritus extends Suoritus {
  // Oppiaineen suorituksella ei ole erillistä vahvistusta - todistuksen vahvistus riittää
  def vahvistus: Option[Vahvistus] = None
}

case class YleissivistäväOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[YleissivistäväOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class YleissivistäväOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def pakollinen: Boolean
}

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite
) extends Laajuus

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("3", Some("Vuosiviikkotuntia"), "opintojenlaajuusyksikko")
) extends Laajuus

case class YleissivistävänkoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkoyleissivistava")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi

object YleissivistävänkoulutuksenArviointi {
  def apply(arvosana: String) = new YleissivistävänkoulutuksenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}
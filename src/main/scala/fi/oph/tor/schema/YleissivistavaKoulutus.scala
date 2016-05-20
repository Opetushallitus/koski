package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

trait OppiaineenSuoritus extends Suoritus {
  // Oppiaineen suorituksella ei ole erillistä vahvistusta - todistuksen vahvistus riittää
  def vahvistus: Option[Vahvistus] = None
  def koulutusmoduuli: YleissivistavaOppiaine
}

case class YleissivistäväOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[YleissivistäväOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class YleissivistäväOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("opiskeluoikeudentila")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def pakollinen: Boolean
}

case class YleissivistävänkoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkoyleissivistava")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends KoodistostaLöytyväArviointi

object YleissivistävänkoulutuksenArviointi {
  def apply(arvosana: String) = new YleissivistävänkoulutuksenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}
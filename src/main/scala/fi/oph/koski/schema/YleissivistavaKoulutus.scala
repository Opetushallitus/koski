package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

trait OppiaineenSuoritus extends Suoritus {
  // Oppiaineen suorituksella ei ole erillistä vahvistusta - todistuksen vahvistus riittää
  def vahvistus: Option[Vahvistus] = None
  def koulutusmoduuli: YleissivistavaOppiaine
}

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def pakollinen: Boolean
}

trait YleissivistävänKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkoyleissivistava")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo match {
    case "H" => false
    case "4" => false
    case _ => true
  }
}
package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

trait OppiaineenSuoritus extends Suoritus {
  def koulutusmoduuli: YleissivistavaOppiaine
}

case class Päätösjakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate]
)

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
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
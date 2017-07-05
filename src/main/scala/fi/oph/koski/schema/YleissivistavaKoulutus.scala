package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.{Description, Title}

trait OppiaineenSuoritus extends Suoritus {
  @Title("Oppiaine")
  def koulutusmoduuli: Koulutusmoduuli with Valinnaisuus
}

trait Yksilöllistettävä {
  @Description("Tieto siitä, onko oppiaineen oppimäärä yksilöllistetty (true/false)")
  def yksilöllistettyOppimäärä: Boolean
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

trait NumeerinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi {
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  def arvosana: Koodistokoodiviite
}

trait SanallinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi with SanallinenArviointi {
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  def arvosana: Koodistokoodiviite
}
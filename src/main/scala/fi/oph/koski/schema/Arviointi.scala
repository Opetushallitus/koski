package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._

trait Arviointi {
  def arvosana: KoodiViite
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  def arviointipäivä: Option[LocalDate]
  def arvioitsijat: Option[List[Arvioitsija]]

  def arvosanaNumeroin: Option[LocalizedString] = {
    try { Some(LocalizedString.unlocalized(arvosana.koodiarvo.toInt.toString)) } catch {
      case e: NumberFormatException => None
    }
  }
  def arvosanaKirjaimin: LocalizedString
  @SyntheticProperty
  @Hidden
  @Description("Onko arviointi hyväksytty")
  def hyväksytty: Boolean
  def description = arvosanaNumeroin.getOrElse(arvosanaKirjaimin)
}

trait ArviointiPäivämäärällä extends Arviointi {
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  def päivä: LocalDate
  def arviointipäivä = Some(päivä)
}

trait KoodistostaLöytyväArviointi extends Arviointi {
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  @Representative
  def arvosana: Koodistokoodiviite
  def arvosanaKirjaimin = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
  @ReadOnly("Tiedon syötössä hyväksytty-tietoa ei tarvita; tieto lasketaan arvosanan perusteella")
  def hyväksytty: Boolean
}

trait PaikallinenArviointi extends Arviointi {
  @Description("Paikallinen arvosana, jota ei löydy kansallisesta koodistosta")
  def arvosana: PaikallinenKoodi
  def arvosanaKirjaimin = arvosana.nimi
}

case class Arvioitsija(
  @Representative
  nimi: String
)

trait SanallinenArviointi extends Arviointi {
  def kuvaus: Option[LocalizedString]
  override def description = kuvaus.getOrElse(super.description)
}
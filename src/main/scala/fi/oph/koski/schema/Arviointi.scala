package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._

trait Arviointi {
  def arvosana: KoodiViite
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  def arviointipäivä: Option[LocalDate]
  def arvioitsijat: Option[List[SuorituksenArvioitsija]]

  def arvosanaNumeroin: Option[LocalizedString] = {
    Arviointi.numeerinen(arvosana.koodiarvo).map(x => LocalizedString.unlocalized(x.toString))
  }
  def arvosanaKirjaimin: LocalizedString
  @SyntheticProperty
  @Hidden
  @Description("Onko arviointi hyväksytty")
  @ReadOnly("Tiedon syötössä arvoa ei tarvita, eikä syötettyä arvoa käsitellä; arvo päätellään arvosanasta.")
  def hyväksytty: Boolean
  def description = arvosanaNumeroin.getOrElse(arvosanaKirjaimin)
}

object Arviointi {
  def numeerinen(arviointi: String) = try { Some(arviointi.toInt) } catch {
    case e: NumberFormatException => None
  }
}

trait ArviointiPäivämäärällä extends Arviointi {
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  @Title("Arviointipäivä")
  def päivä: LocalDate
  def arviointipäivä = Some(päivä)
}

trait KoodistostaLöytyväArviointi extends Arviointi {
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
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

trait SuorituksenArvioitsija {
  @Description("Henkilön koko nimi")
  def nimi: String
}

case class Arvioitsija(
  @Representative
  nimi: String
) extends SuorituksenArvioitsija

trait SanallinenArviointi extends Arviointi {
  @Title("Sanallinen arviointi")
  def kuvaus: Option[LocalizedString]
  override def description = kuvaus.getOrElse(super.description)
}
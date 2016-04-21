package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.localization.LocalizedString.unlocalized
import fi.oph.tor.schema.generic.annotation._

trait Arviointi {
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  def arvosana: Koodistokoodiviite
  @Description("Päivämäärä, jolloin arviointi on annettu")
  def päivä: Option[LocalDate]
  def arvioitsijat: Option[List[Arvioitsija]]

  def arvosanaNumeroin = {
    try { Some(arvosana.koodiarvo.toInt) } catch {
      case e: NumberFormatException => None
    }
  }
  def arvosanaKirjaimin = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
}

case class Arvioitsija(
  nimi: String
)
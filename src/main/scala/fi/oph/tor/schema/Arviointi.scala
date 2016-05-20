package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._


trait Arviointi {
  def arvosana: KoodiViite
  @Description("Päivämäärä, jolloin arviointi on annettu")
  def päivä: Option[LocalDate]
  def arvioitsijat: Option[List[Arvioitsija]]

  def arvosanaNumeroin = {
    try { Some(arvosana.koodiarvo.toInt) } catch {
      case e: NumberFormatException => None
    }
  }
  def arvosanaKirjaimin: LocalizedString
}

trait KoodistostaLöytyväArviointi extends Arviointi {
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  def arvosana: Koodistokoodiviite
  def arvosanaKirjaimin = arvosana.nimi.getOrElse(unlocalized(arvosana.koodiarvo))
}

trait PaikallinenArviointi extends Arviointi {
  @Description("Paikallinen arvosana, jota ei löydy kansallisesta koodistosta")
  def arvosana: Paikallinenkoodi
  def arvosanaKirjaimin = arvosana.nimi
}

case class Arvioitsija(
  nimi: String
)
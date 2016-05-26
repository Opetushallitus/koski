package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.scalaschema.annotation._


trait Arviointi {
  def arvosana: KoodiViite
  @Description("Päivämäärä, jolloin arviointi on annettu")
  def päivä: Option[LocalDate]
  def arvioitsijat: Option[List[Arvioitsija]]

  def arvosanaNumeroin: Option[LocalizedString] = {
    try { Some(LocalizedString.unlocalized(arvosana.koodiarvo.toInt.toString)) } catch {
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
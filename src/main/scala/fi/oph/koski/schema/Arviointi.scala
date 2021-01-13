package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.common.schema.LocalizedString
import fi.oph.common.schema.annotation.Representative
import fi.oph.common.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.{Hidden, MultiLineString}
import fi.oph.scalaschema.annotation._

trait Arviointi {
  def arvosana: KoodiViite
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  def arviointipäivä: Option[LocalDate]
  @Title("Arvioijat")
  def arvioitsijat: Option[List[SuorituksenArvioitsija]]

  def arvosanaNumeroin: Option[LocalizedString] = {
    Arviointi.numeerinen(arvosana.koodiarvo).map(x => LocalizedString.unlocalized(x.toString))
  }
  def arvosanaKirjaimin: LocalizedString
  @SyntheticProperty
  @Hidden
  @Description("Onko arviointi hyväksytty. Tiedon syötössä arvoa ei tarvita, eikä syötettyä arvoa käsitellä; arvo päätellään arvosanasta")
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
  def hyväksytty: Boolean
}

trait PaikallinenArviointi extends Arviointi {
  @Description("Paikallinen arvosana, jota ei löydy kansallisesta koodistosta")
  def arvosana: PaikallinenKoodi
  def arvosanaKirjaimin = arvosana.nimi
}

@Description("Arvioitsijan/arvioitsijoiden tiedot")
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
  @MultiLineString(3)
  def kuvaus: Option[LocalizedString]
  override def description = kuvaus.getOrElse(super.description)
}


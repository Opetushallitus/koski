package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.{Hidden, MultiLineString, Representative}
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
  lazy val longTimeIntoFuture = LocalDate.of(9999, 12, 31)

  def numeerinen(arviointi: String) = try { Some(arviointi.toInt) } catch {
    case e: NumberFormatException => None
  }
  def korkeampiArviointi(a: Arviointi, b: Arviointi) = {
    (numeerinen(a.arvosana.koodiarvo), numeerinen(b.arvosana.koodiarvo)) match {
      case (Some(aNumeerisena), Some(bNumeerisena)) => {
        if (aNumeerisena > bNumeerisena) {
          a
          } else if (aNumeerisena == bNumeerisena &&
              a.arviointipäivä.getOrElse(longTimeIntoFuture)
              .isBefore(b.arviointipäivä.getOrElse(longTimeIntoFuture))) {
            a
          } else {
          b
        }
      }
      case _ => {
        if (a.hyväksytty && !b.hyväksytty) {
          a
        } else if (a.hyväksytty == b.hyväksytty &&
          a.arviointipäivä.getOrElse(longTimeIntoFuture)
          .isBefore(b.arviointipäivä.getOrElse(longTimeIntoFuture))) {
          a
        } else {
          b
        }
      }
    }
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


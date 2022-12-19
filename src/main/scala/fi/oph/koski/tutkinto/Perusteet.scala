package fi.oph.koski.tutkinto

import fi.oph.koski.schema.SuorituksenTyyppi
import fi.oph.koski.schema.SuorituksenTyyppi.SuorituksenTyyppi

object Perusteet {
  val LukionOpetussuunnitelmanPerusteet2019 = Diaarinumero("OPH-2263-2019")
  val AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019 = Diaarinumero("OPH-2267-2019")

  val lops2019 = Diaarinumerot(List(LukionOpetussuunnitelmanPerusteet2019, AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019))

  val TaiteenPerusopetuksenYleisenOppimääränPerusteet2017 = Diaarinumero("OPH-2069-2017")
  val TaiteenPerusopetuksenLaajanOppimääränPerusteet2017 = Diaarinumero("OPH-2068-2017")

  def sallitutPerusteet(suorituksenTyyppi: SuorituksenTyyppi): Diaarinumerorajaus =
    perusteetBySuoritus.getOrElse(suorituksenTyyppi, Kaikki)

  private lazy val perusteetBySuoritus = Map(
    SuorituksenTyyppi.lukionaineopinnot -> lops2019
  )
}

trait Diaarinumerorajaus {
  def matches(str: String): Boolean
}

case object Kaikki extends Diaarinumerorajaus {
  override def matches(str: String): Boolean = true
}

case class Diaarinumero(diaari: String) extends Diaarinumerorajaus {
  override def matches(str: String): Boolean = diaari == str
  override def toString: String = diaari
}

case class Diaarinumerot(diaarit: List[Diaarinumerorajaus]) extends Diaarinumerorajaus {
  override def matches(str: String): Boolean = diaarit.exists(_.matches(str))
  override def toString: String = diaarit.map(_.toString).mkString(", ")
}

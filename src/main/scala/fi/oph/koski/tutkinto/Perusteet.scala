package fi.oph.koski.tutkinto

import fi.oph.koski.schema.SuorituksenTyyppi
import fi.oph.koski.schema.SuorituksenTyyppi.SuorituksenTyyppi

object Perusteet {
  val LukionOpetussuunnitelmanPerusteet2019 = Diaarinumero("OPH-2263-2019")
  val AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019 = Diaarinumero("OPH-2267-2019")

  val lops2019 = Diaarinumerot(List(LukionOpetussuunnitelmanPerusteet2019, AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019))

  def sallitutPerusteet(suorituksenTyyppi: SuorituksenTyyppi): Diaarinumerorajaus =
    perusteetBySuoritus.getOrElse(suorituksenTyyppi, Kaikki)

  private lazy val perusteetBySuoritus = Map(
    SuorituksenTyyppi.lukionoppimaara2019 -> lops2019,
    SuorituksenTyyppi.lukionoppiaineidenoppimaarat2019 -> lops2019
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
}

case class Diaarinumerot(diaarit: List[Diaarinumerorajaus]) extends Diaarinumerorajaus {
  override def matches(str: String): Boolean = diaarit.exists(_.matches(str))
}

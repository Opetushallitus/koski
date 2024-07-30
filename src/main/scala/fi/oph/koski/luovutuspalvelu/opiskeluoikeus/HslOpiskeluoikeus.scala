package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

trait HslOpiskeluoikeus {
  def tyyppi: Koodistokoodiviite

  def oid: String

  def oppilaitos: Option[Oppilaitos]

  def tila: HslOpiskeluoikeudenTila

  def suoritukset: List[HslPäätasonSuoritus]

  def lisätiedot: Option[HslOpiskeluoikeudenLisätiedot]

  def arvioituPäättymispäivä: Option[LocalDate]
}

object HslOpiskeluoikeus {
  def apply(oo: Opiskeluoikeus): Option[HslOpiskeluoikeus] =
    (oo match {
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(HslAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(HslAmmatillinenOpiskeluoikeus(o))
      case o: DIAOpiskeluoikeus => Some(HslDiaOpiskeluoikeus(o))
      case o: IBOpiskeluoikeus => Some(HslIBOpiskeluoikeus(o))
      case o: InternationalSchoolOpiskeluoikeus => Some(HslInternationalSchoolOpiskeluoikeus(o))
      case o: KorkeakoulunOpiskeluoikeus => Some(HslKorkeakoulunOpiskeluoikeus(o))
      case o: LukionOpiskeluoikeus => Some(HslLukionOpiskeluoikeus(o))
      case o: LukioonValmistavanKoulutuksenOpiskeluoikeus => Some(HslLukioonValmistavanKoulutuksenOpiskeluoikeus(o))
      case o: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => Some(HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenLisäopetuksenOpiskeluoikeus => Some(HslPerusopetuksenLisäopetuksenOpiskeluoikeus(o))
      case o: PerusopetuksenOpiskeluoikeus => Some(HslPerusopetuksenOpiskeluoikeus(o))
      case o: YlioppilastutkinnonOpiskeluoikeus => Some(HslYlioppilastutkinnonOpiskeluoikeus(o))
      case _ => None
    })
}

case class HslOpiskeluoikeudenTila(
  opiskeluoikeusJaksot: Option[List[HslOpiskeluoikeusJakso]]
)

object HslOpiskeluoikeudenTila {
  def apply(t: OpiskeluoikeudenTila): HslOpiskeluoikeudenTila = HslOpiskeluoikeudenTila(Some(t.opiskeluoikeusjaksot.map(j => HslOpiskeluoikeusJakso(j.tila, j.alku, j.opiskeluoikeusPäättynyt))))
}

case class HslOpiskeluoikeusJakso(
  tila: Koodistokoodiviite,
  alku: LocalDate,
  opiskeluoikeusPäättynyt: Boolean
)

trait HslPäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
}

@Title("Päätason suoritus")
case class HslDefaultPäätasonSuoritus(
  tyyppi: Koodistokoodiviite
) extends HslPäätasonSuoritus

object HslDefaultPäätasonSuoritus {
  def apply(s: Suoritus): HslDefaultPäätasonSuoritus = HslDefaultPäätasonSuoritus(
    tyyppi = s.tyyppi
  )
}

@Title("Opiskeluoikeuden lisätiedot")
trait HslOpiskeluoikeudenLisätiedot {
  def osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]]
}

case class HslDefaultOpiskeluoikeudenLisätiedot(
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None
) extends HslOpiskeluoikeudenLisätiedot

object HslDefaultOpiskeluoikeudenLisätiedot {
  def apply(l: OpiskeluoikeudenLisätiedot): HslDefaultOpiskeluoikeudenLisätiedot = l match {
    case x: OsaAikaisuusjaksollinen => HslDefaultOpiskeluoikeudenLisätiedot(osaAikaisuusjaksot = x.osaAikaisuusjaksot)
    case _ => HslDefaultOpiskeluoikeudenLisätiedot()
  }
}


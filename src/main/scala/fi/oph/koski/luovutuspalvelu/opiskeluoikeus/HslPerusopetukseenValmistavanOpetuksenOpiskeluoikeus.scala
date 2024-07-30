package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos, PerusopetukseenValmistavanOpetuksenOpiskeluoikeus}
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Perusopetukseen valmistavan opetuksen opiskeluoikeus")
case class HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot] = None,
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus {
  def apply(oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus): HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = None,
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

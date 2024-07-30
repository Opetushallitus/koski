package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos, PerusopetuksenLisäopetuksenOpiskeluoikeus}
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Perusopetuksen lisäopetuksen opiskeluoikeus")
case class HslPerusopetuksenLisäopetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate]
) extends HslOpiskeluoikeus

object HslPerusopetuksenLisäopetuksenOpiskeluoikeus {
  def apply(oo: PerusopetuksenLisäopetuksenOpiskeluoikeus): HslPerusopetuksenLisäopetuksenOpiskeluoikeus = HslPerusopetuksenLisäopetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä
  )
}

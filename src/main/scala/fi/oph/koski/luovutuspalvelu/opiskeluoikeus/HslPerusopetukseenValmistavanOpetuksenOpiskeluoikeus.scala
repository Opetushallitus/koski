package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{Koodistokoodiviite, OpiskeluoikeudenOrganisaatiohistoria, Oppilaitos, PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, SisältäväOpiskeluoikeus}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetukseen valmistavan opetuksen opiskeluoikeus")
case class HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot] = None,
  arvioituPäättymispäivä: Option[LocalDate],
  aikaleima: Option[LocalDateTime],
  alkamispäivä: Option[LocalDate],
  versionumero: Option[Int],
  päättymispäivä: Option[LocalDate],
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
) extends HslOpiskeluoikeus

object HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus {
  def apply(oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus): HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = HslPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = None,
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
    aikaleima = oo.aikaleima,
    alkamispäivä = oo.alkamispäivä,
    versionumero = oo.versionumero,
    päättymispäivä = oo.päättymispäivä,
    organisaatiohistoria = oo.organisaatiohistoria.map(x => x.map(HslOpiskeluoikeudenOrganisaatiohistoria.apply)),
    sisältyyOpiskeluoikeuteen = oo.sisältyyOpiskeluoikeuteen
  )
}

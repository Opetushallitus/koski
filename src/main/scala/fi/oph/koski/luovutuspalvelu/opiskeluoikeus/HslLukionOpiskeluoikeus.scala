package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{Koodistokoodiviite, LukionOpiskeluoikeus, OpiskeluoikeudenOrganisaatiohistoria, Oppilaitos}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Lukion opiskeluoikeus")
case class HslLukionOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslDefaultOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate],
  aikaleima: Option[LocalDateTime],
  alkamispäivä: Option[LocalDate],
  versionumero: Option[Int],
  päättymispäivä: Option[LocalDate],
  oppimääräSuoritettu: Option[Boolean] = None,
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]]
) extends HslOpiskeluoikeus

object HslLukionOpiskeluoikeus {
  def apply(oo: LukionOpiskeluoikeus): HslLukionOpiskeluoikeus = HslLukionOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
    aikaleima = oo.aikaleima,
    alkamispäivä = oo.alkamispäivä,
    versionumero = oo.versionumero,
    päättymispäivä = oo.päättymispäivä,
    oppimääräSuoritettu = oo.oppimääräSuoritettu,
    organisaatiohistoria = oo.organisaatiohistoria.map(x => x.map(HslOpiskeluoikeudenOrganisaatiohistoria.apply))
  )
}

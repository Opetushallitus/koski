package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{Koodistokoodiviite, LukioonValmistavanKoulutuksenOpiskeluoikeus, OpiskeluoikeudenOrganisaatiohistoria, Oppilaitos}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Lukioon valmistavan koulutuksen opiskeluoikeus")
case class HslLukioonValmistavanKoulutuksenOpiskeluoikeus(
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
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]]
) extends HslOpiskeluoikeus

object HslLukioonValmistavanKoulutuksenOpiskeluoikeus {
  def apply(oo: LukioonValmistavanKoulutuksenOpiskeluoikeus): HslLukioonValmistavanKoulutuksenOpiskeluoikeus = HslLukioonValmistavanKoulutuksenOpiskeluoikeus(
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
    organisaatiohistoria = oo.organisaatiohistoria.map(x => x.map(HslOpiskeluoikeudenOrganisaatiohistoria.apply))
  )
}

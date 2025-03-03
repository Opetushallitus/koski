package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, InternationalSchoolOpiskeluoikeus, Koodistokoodiviite, Oppilaitos, SisältäväOpiskeluoikeus}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("European School of Helsinki -opiskeluoikeus")
case class HslEuropeanSchoolOfHelsinkiOpiskeluoikeus(
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
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
) extends HslOpiskeluoikeus

object HslEuropeanSchoolOfHelsinkiOpiskeluoikeus {
  def apply(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): HslEuropeanSchoolOfHelsinkiOpiskeluoikeus = HslEuropeanSchoolOfHelsinkiOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.getOrElse(""),
    oppilaitos = oo.oppilaitos,
    tila = HslOpiskeluoikeudenTila.apply(oo.tila),
    suoritukset = oo.suoritukset.filter { x => List("S5", "S6", "S7").contains(x.koulutusmoduuli.tunniste.koodiarvo) }.map(HslDefaultPäätasonSuoritus.apply),
    lisätiedot = oo.lisätiedot.map(HslDefaultOpiskeluoikeudenLisätiedot.apply),
    arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
    aikaleima = oo.aikaleima,
    alkamispäivä = oo.alkamispäivä,
    versionumero = oo.versionumero,
    päättymispäivä = oo.päättymispäivä,
    organisaatiohistoria = oo.organisaatiohistoria.map(x => x.map(HslOpiskeluoikeudenOrganisaatiohistoria.apply)),
    sisältyyOpiskeluoikeuteen = oo.sisältyyOpiskeluoikeuteen
  )
}

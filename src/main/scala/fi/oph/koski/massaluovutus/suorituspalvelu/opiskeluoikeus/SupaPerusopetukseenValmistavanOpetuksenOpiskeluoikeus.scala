package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetukseen valmistavan opetuksen opiskeluoikeus")
case class SupaPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaPerusopetukseenValmistavanOpetuksenSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
) extends SupaOpiskeluoikeus

object SupaPerusopetukseenValmistavanOpetuksenOpiskeluoikeus {
  def apply(oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, oppijaOid: String): SupaPerusopetukseenValmistavanOpetuksenOpiskeluoikeus =
    SupaPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      sisältyyOpiskeluoikeuteen = oo.sisältyyOpiskeluoikeuteen,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      alkamispäivä = oo.alkamispäivä,
      päättymispäivä = oo.päättymispäivä,
      suoritukset = oo.suoritukset.map(SupaPerusopetukseenValmistavanOpetuksenSuoritus.apply),
      versionumero = oo.versionumero,
      aikaleima = oo.aikaleima,
    )
}

@Title("Perusopetukseen valmistavan opetuksen suoritus")
case class SupaPerusopetukseenValmistavanOpetuksenSuoritus(
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: PerusopetukseenValmistavaOpetus,
  vahvistus: Option[SupaVahvistus],
  suorituskieli: Koodistokoodiviite,
) extends SupaSuoritus with SupaVahvistuksellinen

object SupaPerusopetukseenValmistavanOpetuksenSuoritus {
  def apply(s: PerusopetukseenValmistavanOpetuksenSuoritus): SupaPerusopetukseenValmistavanOpetuksenSuoritus =
    SupaPerusopetukseenValmistavanOpetuksenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      suorituskieli = s.suorituskieli,
    )
}

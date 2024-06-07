package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.util.CaseClass
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate


@Title("Opiskeluoikeus")
case class SureOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SurePäätasonSuoritus],
  lisätiedot: Option[SureOpiskeluoikeudenLisätiedot],
)

object SureOpiskeluoikeus {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(oo, oo.suoritukset.map(SureDefaultPäätasonSuoritus.apply))

  def apply(
    oo: KoskeenTallennettavaOpiskeluoikeus,
    suoritukset: List[SurePäätasonSuoritus],
    lisätiedot: Option[SureOpiskeluoikeudenLisätiedot] = None,
  ): SureOpiskeluoikeus =
    SureOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = suoritukset,
      lisätiedot = lisätiedot,
    )
}

trait SureOpiskeluoikeudenLisätiedot

trait SurePäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: Koulutusmoduuli
}

@Title("Muu päätason suoritus")
case class SureDefaultPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: Koulutusmoduuli,
  suorituskieli: Option[Koodistokoodiviite],
  osasuoritukset: Option[List[SureOsasuoritus]],
) extends SurePäätasonSuoritus

object SureDefaultPäätasonSuoritus {
  def apply(s: Suoritus): SureDefaultPäätasonSuoritus =
    SureDefaultPäätasonSuoritus(s, s.osasuoritukset.map(_.map(SureDefaultOsasuoritus.apply)))

  def apply(s: Suoritus, osasuoritukset: Option[List[SureOsasuoritus]]): SureDefaultPäätasonSuoritus =
    SureDefaultPäätasonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = CaseClass.getFieldValue(s, "suorituskieli"),
      osasuoritukset = osasuoritukset,
    )
}

trait SureOsasuoritus

@Title("Muu osasuoritus")
case class SureDefaultOsasuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: Koulutusmoduuli,
  arviointi: Option[List[Arviointi]],
) extends SureOsasuoritus

object SureDefaultOsasuoritus {
  def apply(os: Suoritus): SureDefaultOsasuoritus =
    SureDefaultOsasuoritus(
      tyyppi = os.tyyppi,
      koulutusmoduuli = os.koulutusmoduuli,
      arviointi = os.arviointi.map(_.sortBy(a => a.arviointipäivä)),
    )
}

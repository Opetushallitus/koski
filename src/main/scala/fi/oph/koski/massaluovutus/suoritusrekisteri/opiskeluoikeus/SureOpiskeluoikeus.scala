package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.koski.util.CaseClass

import java.time.LocalDate


case class SureDefaultOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SurePäätasonSuoritus],
) extends SureOpiskeluoikeus

object SureDefaultOpiskeluoikeus {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): SureDefaultOpiskeluoikeus =
    SureDefaultOpiskeluoikeus(oo, oo.suoritukset.map(SureDefaultPäätasonSuoritus.apply))

  def apply(oo: KoskeenTallennettavaOpiskeluoikeus, suoritukset: List[SurePäätasonSuoritus]): SureDefaultOpiskeluoikeus =
    SureDefaultOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = suoritukset,
    )
}

trait SurePäätasonSuoritus {
  def tyyppi: Koodistokoodiviite
  def koulutusmoduuli: Koulutusmoduuli
}

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

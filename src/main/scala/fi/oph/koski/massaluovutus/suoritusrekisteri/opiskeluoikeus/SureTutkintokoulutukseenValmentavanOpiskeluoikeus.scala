package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate


@Title("Tutkintokoulutukseen valmentava koulutus")
case class SureTutkintokoulutukseenValmentavanOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.tuva.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila,
  suoritukset: List[SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus]
) extends SureOpiskeluoikeus

object SureTutkintokoulutukseenValmentavanOpiskeluoikeus {
  def apply(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus): SureOpiskeluoikeus =
    SureTutkintokoulutukseenValmentavanOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.collect {
        case s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus => SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus(s)
      }
    )
}

@Title("TUVA-koulutuksen suoritus")
case class SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SureVahvistus],
  koulutusmoduuli:  TutkintokoulutukseenValmentavanKoulutus,
) extends SureSuoritus with SureVahvistuksellinen

object SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus {
  def apply(s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus): SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus =
    SureTutkintokoulutukseenValmentavanKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SureVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
    )
}

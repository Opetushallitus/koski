package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Ammatillinen koulutus")
case class SureAmmatillinenOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SureAmmatillinenPäätasonSuoritus],
) extends SureOpiskeluoikeus

object SureAmmatillinenTutkinto {
  def apply(oo: AmmatillinenOpiskeluoikeus): SureAmmatillinenOpiskeluoikeus =
    SureAmmatillinenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.collect {
        case s: AmmatillisenTutkinnonSuoritus => SureAmmatillisenTutkinnonSuoritus(s)
        case s: TelmaKoulutuksenSuoritus => SureTelmaKoulutuksenSuoritus(s)
      }
    )
}

trait SureAmmatillinenPäätasonSuoritus extends SureSuoritus

@Title("Ammatillisen tutkinnon suoritus")
case class SureAmmatillisenTutkinnonSuoritus(
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suorituskieli: Koodistokoodiviite,
) extends SureAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureAmmatillisenTutkinnonSuoritus {
  def apply(s: AmmatillisenTutkinnonSuoritus): SureAmmatillisenTutkinnonSuoritus =
    SureAmmatillisenTutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
    )
}

@Title("TELMA-koulutuksen suoritus")
case class SureTelmaKoulutuksenSuoritus(
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: TelmaKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureTelmaKoulutuksenOsanSuoritus],
) extends SureAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureTelmaKoulutuksenSuoritus {
  def apply(s: TelmaKoulutuksenSuoritus): SureTelmaKoulutuksenSuoritus =
    SureTelmaKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SureTelmaKoulutuksenOsanSuoritus.apply),
    )
}

@Title("TELMA-koulutuksen osan suoritus")
case class SureTelmaKoulutuksenOsanSuoritus(
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TelmaKoulutuksenOsa,
  arviointi: Option[List[TelmaJaValmaArviointi]],
) extends SureSuoritus

object SureTelmaKoulutuksenOsanSuoritus {
  def apply(s: TelmaKoulutuksenOsanSuoritus): SureTelmaKoulutuksenOsanSuoritus =
    SureTelmaKoulutuksenOsanSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi
    )
}

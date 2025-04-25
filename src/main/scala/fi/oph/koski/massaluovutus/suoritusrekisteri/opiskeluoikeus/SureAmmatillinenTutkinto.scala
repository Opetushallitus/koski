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
  tila: AmmatillinenOpiskeluoikeudenTila,
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
        case s: YhteisenTutkinnonOsanSuoritus => SureYhteisenTutkinnonOsanSuoritus(s)
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
  osasuoritukset: List[SureAmmatillisenTutkinnonOsasuoritus],
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
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SureAmmatillisenTutkinnonOsasuoritus.apply),
    )
}

@Title("Ammatillisen tutkinnon osan suoritus")
case class SureAmmatillisenTutkinnonOsasuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureSuoritus

object SureAmmatillisenTutkinnonOsasuoritus {
  def apply(s: AmmatillisenTutkinnonOsanSuoritus): SureAmmatillisenTutkinnonOsasuoritus =
    SureAmmatillisenTutkinnonOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
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

@Title("Yhteisen tutkinnon suoritus")
case class SureYhteisenTutkinnonOsanSuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: YhteinenTutkinnonOsa,
  suorituskieli: Option[Koodistokoodiviite],
  osasuoritukset: List[SureYhteisenTutkinnonOsanOsasuoritus],
) extends SureAmmatillinenPäätasonSuoritus
  with MahdollisestiSuorituskielellinen
  with Vahvistuspäivällinen

object SureYhteisenTutkinnonOsanSuoritus {
  def apply(s: YhteisenTutkinnonOsanSuoritus): SureYhteisenTutkinnonOsanSuoritus = {
    s match {
      case ss: YhteisenAmmatillisenTutkinnonOsanSuoritus => SureYhteisenTutkinnonOsanSuoritus(
        tyyppi = ss.tyyppi,
        alkamispäivä = ss.alkamispäivä,
        vahvistuspäivä = ss.vahvistus.map(_.päivä),
        koulutusmoduuli = ss.koulutusmoduuli,
        suorituskieli = ss.suorituskieli,
        osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureYhteisenTutkinnonOsanOsasuoritus.apply),
      )
      case ss: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => SureYhteisenTutkinnonOsanSuoritus(
        tyyppi = ss.tyyppi,
        alkamispäivä = ss.alkamispäivä,
        vahvistuspäivä = ss.vahvistus.map(_.päivä),
        koulutusmoduuli = ss.koulutusmoduuli,
        suorituskieli = ss.suorituskieli,
        osasuoritukset = ss.osasuoritukset.toList.flatten.map(SureYhteisenTutkinnonOsanOsasuoritus.apply),
      )
    }
  }
}

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
case class SureYhteisenTutkinnonOsanOsasuoritus(
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]],
) extends SureSuoritus

object SureYhteisenTutkinnonOsanOsasuoritus {
  def apply(s: YhteisenTutkinnonOsanOsaAlueenSuoritus): SureYhteisenTutkinnonOsanOsasuoritus =
    SureYhteisenTutkinnonOsanOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

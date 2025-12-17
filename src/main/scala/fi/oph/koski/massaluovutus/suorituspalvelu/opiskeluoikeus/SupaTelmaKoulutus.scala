package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{ComplexObject, KoodistoKoodiarvo, Tooltip}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate


@Title("TELMA-koulutuksen suoritus")
@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class SupaTelmaKoulutuksenSuoritus(
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: TelmaKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SupaTelmaKoulutuksenOsanSuoritus]],
) extends SupaAmmatillinenPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaTelmaKoulutuksenSuoritus {
  def apply(s: TelmaKoulutuksenSuoritus): SupaTelmaKoulutuksenSuoritus =
    SupaTelmaKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.map(SupaTelmaKoulutuksenOsanSuoritus.apply)).filter(_.nonEmpty),
    )
}

@Title("TELMA-koulutuksen osan suoritus")
@Description("Suoritettavan TELMA-koulutuksen osan tiedot")
case class SupaTelmaKoulutuksenOsanSuoritus(
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TelmaKoulutuksenOsa,
  arviointi: Option[List[TelmaJaValmaArviointi]],
) extends SupaSuoritus

object SupaTelmaKoulutuksenOsanSuoritus {
  def apply(s: TelmaKoulutuksenOsanSuoritus): SupaTelmaKoulutuksenOsanSuoritus =
    SupaTelmaKoulutuksenOsanSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi
    )
}


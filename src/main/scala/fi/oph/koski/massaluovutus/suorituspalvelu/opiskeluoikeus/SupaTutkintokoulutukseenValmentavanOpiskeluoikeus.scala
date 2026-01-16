package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus")
case class SupaTutkintokoulutukseenValmentavanOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.tuva.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
) extends SupaOpiskeluoikeus

object SupaTutkintokoulutukseenValmentavanOpiskeluoikeus {
  def apply(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus, oppijaOid: String): SupaOpiskeluoikeus =
    SupaTutkintokoulutukseenValmentavanOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      alkamispäivä = oo.alkamispäivä,
      päättymispäivä = oo.päättymispäivä,
      suoritukset = oo.suoritukset.collect {
        case s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus => SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus(s)
      },
      versionumero = oo.versionumero,
      aikaleima = oo.aikaleima
    )
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen suoritustiedot")
case class SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus(
  @KoodistoKoodiarvo("tuvakoulutuksensuoritus")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus,
  osasuoritukset : Option[List[SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus]]
) extends SupaSuoritus with SupaVahvistuksellinen

object SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus {
  def apply(s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus): SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus =
    SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map(_.map(SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus.apply)).filter(_.nonEmpty)
    )
}

trait SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus extends SupaSuoritus {
  def arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]]
}

object SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus {
  def apply(os: TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus): SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus = os match {
    case os: TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus => SupaTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(os)
    case os: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus => SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(os)
  }
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
case class SupaTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]]
) extends SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus

object SupaTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus {
  def apply(os: TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus): SupaTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus =
    SupaTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus (
      tyyppi = os.tyyppi,
      koulutusmoduuli =  os.koulutusmoduuli,
      arviointi = os.arviointi
    )
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus")
case class SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa,
  arviointi: Option[List[SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]],
  osasuoritukset: Option[List[SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus]]
) extends SupaTutkintokoulutukseenValmentevanKoulutuksenOsasuoritus

object SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus {
  def apply(os: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus): SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
    SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(
      tyyppi = os.tyyppi,
      koulutusmoduuli = os.koulutusmoduuli,
      arviointi = os.arviointi,
      osasuoritukset = os.osasuoritukset
        .map(_.map(SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus.apply))
        .filter(_.nonEmpty)
    )
}

@Title("Valinnaisten tutkintokoulutukseen valmentavan koulutuksen opintojen osasuoritukset")
case class SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus,
  arviointi: Option[List[TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi]]
) extends SupaSuoritus

object SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus {
  def apply(os: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus): SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
    SupaTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
      tyyppi = os.tyyppi,
      koulutusmoduuli = os.koulutusmoduuli,
      arviointi = os.arviointi
    )
}

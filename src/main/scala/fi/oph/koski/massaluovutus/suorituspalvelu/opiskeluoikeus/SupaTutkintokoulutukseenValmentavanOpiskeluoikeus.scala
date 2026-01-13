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
  koulutusmoduuli:  TutkintokoulutukseenValmentavanKoulutus,
) extends SupaSuoritus with SupaVahvistuksellinen

object SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus {
  def apply(s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus): SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus =
    SupaTutkintokoulutukseenValmentavanKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
    )
}

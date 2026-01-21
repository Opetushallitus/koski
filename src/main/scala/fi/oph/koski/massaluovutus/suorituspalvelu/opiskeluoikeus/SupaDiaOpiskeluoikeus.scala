package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.massaluovutus.suorituspalvelu.SupaUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("DIA-tutkinnon opiskeluoikeus")
case class SupaDIAOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: DIAOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaDIATutkinnonSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
) extends SupaOpiskeluoikeus

object SupaDIAOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus, oppijaOid: String): Option[SupaDIAOpiskeluoikeus] =
    when (isValmistunut(oo)) {
      SupaDIAOpiskeluoikeus(
        oppijaOid = oppijaOid,
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        alkamispäivä = oo.alkamispäivä,
        päättymispäivä = oo.päättymispäivä,
        suoritukset = oo.suoritukset.collect {
          case tutkinnonSuoritus: DIATutkinnonSuoritus if tutkinnonSuoritus.valmis => SupaDIATutkinnonSuoritus(tutkinnonSuoritus)
        },
        versionumero = oo.versionumero,
        aikaleima = oo.aikaleima
      )
    }
}

@Title("DIA-tutkinnon suoritus")
case class SupaDIATutkinnonSuoritus(
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: DIATutkinto,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SupaDIAOppiaineenTutkintovaiheenSuoritus]],
) extends SupaSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaDIATutkinnonSuoritus {
  def apply(s: DIATutkinnonSuoritus): SupaDIATutkinnonSuoritus =
    SupaDIATutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.filter(_.valmis).map(SupaDIAOppiaineenTutkintovaiheenSuoritus.apply)).filter(_.nonEmpty)
    )
}

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class SupaDIAOppiaineenTutkintovaiheenSuoritus(
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: DIAOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  osasuoritukset: Option[List[SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus]],
) extends SupaSuoritus
  with MahdollisestiSuorituskielellinen

object SupaDIAOppiaineenTutkintovaiheenSuoritus {
  def apply(s: DIAOppiaineenTutkintovaiheenSuoritus): SupaDIAOppiaineenTutkintovaiheenSuoritus =
    SupaDIAOppiaineenTutkintovaiheenSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      vastaavuustodistuksenTiedot = s.vastaavuustodistuksenTiedot,
      koetuloksenNelinkertainenPistemäärä = s.koetuloksenNelinkertainenPistemäärä,
      tyyppi = s.tyyppi,
      osasuoritukset = s.osasuoritukset.map(_.map(s => SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(s))).filter(_.nonEmpty),
    )
}

@Title("DIA-tutkinnon päättökoe")
case class SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus,
  arviointi: Option[List[Arviointi]],
) extends SupaSuoritus

object SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus {
  def apply(s: DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus): SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus =
    SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi,
    )
}

package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.massaluovutus.suorituspalvelu.SupaUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("DIA-tutkinnon opiskeluoikeus")
case class SupaDIAOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: DIAOpiskeluoikeudenTila,
  suoritukset: List[SupaDIATutkinnonSuoritus],
) extends SupaOpiskeluoikeus

object SupaDIAOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus): Option[SupaDIAOpiskeluoikeus] =
    when (isValmistunut(oo)) {
      SupaDIAOpiskeluoikeus(
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.collect {
          case tutkinnonSuoritus: DIATutkinnonSuoritus if tutkinnonSuoritus.valmis => SupaDIATutkinnonSuoritus(tutkinnonSuoritus)
        }
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
  osasuoritukset: List[SupaDIAOppiaineenTutkintovaiheenSuoritus],
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
      osasuoritukset =
        s.osasuoritukset
          .toList
          .flatten
          .filter(_.valmis)
          .map(SupaDIAOppiaineenTutkintovaiheenSuoritus.apply)
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
  osasuoritukset: List[SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus],
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
      osasuoritukset = s.osasuoritukset.toList.flatten.flatMap(s => SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(s)),
    )
}

@Title("DIA-tutkinnon päättökoe")
case class SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: DIAPäättökoe,
  arviointi: Option[List[Arviointi]],
) extends SupaSuoritus

object SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus {
  def apply(s: DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus): Option[SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus] = {
    s.koulutusmoduuli match {
      case koulutusmoduuli: DIAPäättökoe =>
        Some(SupaDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
          tyyppi = s.tyyppi,
          koulutusmoduuli = koulutusmoduuli,
          arviointi = s.arviointi,
        ))
      case _ => None
    }
  }
}

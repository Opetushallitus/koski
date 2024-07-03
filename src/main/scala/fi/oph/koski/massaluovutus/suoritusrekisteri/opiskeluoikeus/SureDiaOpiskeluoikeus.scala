package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("DIA-tutkinto")
case class SureDIAOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: DIAOpiskeluoikeudenTila,
  suoritukset: List[SureDIATutkinnonSuoritus],
) extends SureOpiskeluoikeus

object SureDIAOpiskeluoikeus {
  def apply(oo: DIAOpiskeluoikeus): Option[SureDIAOpiskeluoikeus] =
    when (isValmistunut(oo)) {
      SureDIAOpiskeluoikeus(
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.collect {
          case tutkinnonSuoritus: DIATutkinnonSuoritus if tutkinnonSuoritus.valmis => SureDIATutkinnonSuoritus(tutkinnonSuoritus)
        }
      )
    }
}

@Title("DIA-tutkinnon suoritus")
case class SureDIATutkinnonSuoritus(
  @KoodistoKoodiarvo("diatutkintovaihe")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: DIATutkinto,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureDIAOppiaineenTutkintovaiheenSuoritus],
) extends SureSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureDIATutkinnonSuoritus {
  def apply(s: DIATutkinnonSuoritus): SureDIATutkinnonSuoritus =
    SureDIATutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset =
        s.osasuoritukset
          .toList
          .flatten
          .filter(_.valmis)
          .map(SureDIAOppiaineenTutkintovaiheenSuoritus.apply)
    )
}

@Title("DIA-oppiaineen tutkintovaiheen suoritus")
case class SureDIAOppiaineenTutkintovaiheenSuoritus(
  @KoodistoKoodiarvo("diaoppiaine")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: DIAOppiaine,
  suorituskieli: Option[Koodistokoodiviite] = None,
  vastaavuustodistuksenTiedot: Option[DIAVastaavuustodistuksenTiedot] = None,
  koetuloksenNelinkertainenPistemäärä: Option[Int] = None,
  osasuoritukset: List[SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus],
) extends SureSuoritus
  with MahdollisestiSuorituskielellinen

object SureDIAOppiaineenTutkintovaiheenSuoritus {
  def apply(s: DIAOppiaineenTutkintovaiheenSuoritus): SureDIAOppiaineenTutkintovaiheenSuoritus =
    SureDIAOppiaineenTutkintovaiheenSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      vastaavuustodistuksenTiedot = s.vastaavuustodistuksenTiedot,
      koetuloksenNelinkertainenPistemäärä = s.koetuloksenNelinkertainenPistemäärä,
      tyyppi = s.tyyppi,
      osasuoritukset = s.osasuoritukset.toList.flatten.flatMap(s => SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(s)),
    )
}

@Title("DIA-tutkinnon päättökoe")
case class SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
  @KoodistoKoodiarvo("diaoppiaineentutkintovaiheenosasuorituksensuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: DIAPäättökoe,
  arviointi: Option[List[Arviointi]],
) extends SureSuoritus

object SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus {
  def apply(s: DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus): Option[SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus] = {
    s.koulutusmoduuli match {
      case koulutusmoduuli: DIAPäättökoe =>
        Some(SureDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
          tyyppi = s.tyyppi,
          koulutusmoduuli = koulutusmoduuli,
          arviointi = s.arviointi,
        ))
      case _ => None
    }
  }
}

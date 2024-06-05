package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema.{EBOpiskeluoikeus, EBOppiaineenAlaosasuoritus, EBTutkinnonOsasuoritus, EBTutkinnonSuoritus, EBTutkinto, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, SecondaryOppiaine}

import java.time.LocalDate

object SureEBOpiskeluoikeus {
  def apply(oo: EBOpiskeluoikeus): SureOpiskeluoikeus =
    SureDefaultOpiskeluoikeus(
      oo,
      oo.suoritukset.map(SureEBPäätasonSuoritus.apply),
    )
}

case class SureEBPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: EBTutkinto,
  yleisarvosana: Option[Double],
  osasuoritukset: Option[List[SureEBOppiaine]],
) extends SurePäätasonSuoritus

object SureEBPäätasonSuoritus {
  def apply(pts: EBTutkinnonSuoritus): SureEBPäätasonSuoritus =
    SureEBPäätasonSuoritus(
      tyyppi = pts.tyyppi,
      vahvistuspäivä = pts.vahvistus.map(_.päivä),
      koulutusmoduuli = pts.koulutusmoduuli,
      yleisarvosana = pts.yleisarvosana,
      osasuoritukset = pts.osasuoritukset.map(_.map(SureEBOppiaine.apply)),
    )
}
case class SureEBOppiaine(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: SecondaryOppiaine,
  osasuoritukset: Option[List[SureOsasuoritus]],
) extends SureOsasuoritus

object SureEBOppiaine {
  def apply (s: EBTutkinnonOsasuoritus): SureEBOppiaine =
    SureEBOppiaine(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map { _.collect {
        case s: EBOppiaineenAlaosasuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "Final" => SureDefaultOsasuoritus(s)
      }}
    )
}

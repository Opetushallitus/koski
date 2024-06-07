package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object SureEBOpiskeluoikeus {
  def apply(oo: EBOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(
      oo,
      oo.suoritukset.map(SureEBPäätasonSuoritus.apply),
    )
}

@Title("EB-tutkinnon päätason suoritus")
case class SureEBPäätasonSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: EBTutkinto,
  yleisarvosana: Option[Double],
  osasuoritukset: Option[List[SureEBOppiaine]],
) extends SurePäätasonSuoritus

object SureEBPäätasonSuoritus {
  def apply(pts: EBTutkinnonSuoritus): SureEBPäätasonSuoritus =
    SureEBPäätasonSuoritus(
      tyyppi = pts.tyyppi,
      alkamispäivä = pts.alkamispäivä,
      vahvistuspäivä = pts.vahvistus.map(_.päivä),
      koulutusmoduuli = pts.koulutusmoduuli,
      yleisarvosana = pts.yleisarvosana,
      osasuoritukset = pts.osasuoritukset.map(_.map(SureEBOppiaine.apply)),
    )
}
@Title("EB-tutkinnon oppiaine")
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

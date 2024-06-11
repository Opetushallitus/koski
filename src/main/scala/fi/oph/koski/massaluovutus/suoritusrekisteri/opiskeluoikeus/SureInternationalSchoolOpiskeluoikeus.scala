package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object SureInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SureOpiskeluoikeus(
        oo,
        oo.suoritukset.collect {
          case s: DiplomaVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "12" => SureDiplomaVuosiluokanSuoritus(s)
        }
      )
    }
}

object SureDiplomaVuosiluokanSuoritus {
  def apply(s: DiplomaVuosiluokanSuoritus): SurePäätasonSuoritus =
    SureDefaultPäätasonSuoritus(
      s,
      osasuoritukset = s.osasuoritukset.map(_.map(SureDiplomaIBOppiaineenSuoritus.apply)),
    )
}

@Title("International School Diploma-vuosiluokan oppiaineen suoritus")
case class SureDiplomaIBOppiaineenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: Koulutusmoduuli,
) extends SureOsasuoritus

object SureDiplomaIBOppiaineenSuoritus {
  def apply(s: DiplomaIBOppiaineenSuoritus): SureDiplomaIBOppiaineenSuoritus =
    SureDiplomaIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      // TODO TOR-2154: Predicted-arvosanan palautus vaatii sen toteutuksen Diploma IB:n tietomalliin
    )
}

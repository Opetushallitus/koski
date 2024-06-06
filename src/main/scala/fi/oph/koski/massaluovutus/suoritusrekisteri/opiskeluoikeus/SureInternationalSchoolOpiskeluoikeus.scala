package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.util.Option.when

import java.time.LocalDate

object SureInternationalSchoolOpiskeluoikeus {
  def apply(oo: InternationalSchoolOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SureDefaultOpiskeluoikeus(
        oo,
        oo.suoritukset.collect {
          case s: DiplomaVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "12" => SureDiplomaVuosiluokanSuoritus(s)
        }
      )
    }
}

case class SureDiplomaVuosiluokanSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: DiplomaLuokkaAste,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SureDiplomaIBOppiaineenSuoritus]],
) extends SurePäätasonSuoritus

object SureDiplomaVuosiluokanSuoritus {
  def apply(s: DiplomaVuosiluokanSuoritus): SureDiplomaVuosiluokanSuoritus =
    SureDiplomaVuosiluokanSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.map(SureDiplomaIBOppiaineenSuoritus.apply)),
    )
}

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

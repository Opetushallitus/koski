package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object SureIBOpiskeluoikeus {
  def apply(oo: IBOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SureOpiskeluoikeus(
        oo,
        oo.suoritukset.collect { case s: IBTutkinnonSuoritus => SureIBTutkinnonSuoritus(s) }
      )
    }
}

@Title("IB-tutkinnon päätason suoritus")
case class SureIBTutkinnonSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: IBTutkinto,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SureIBOppiaineenSuoritus]],
  theoryOfKnowledgeSuoritus: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  lisäpisteet: Option[Koodistokoodiviite],
) extends SurePäätasonSuoritus

object SureIBTutkinnonSuoritus {
  def apply(s: IBTutkinnonSuoritus): SureIBTutkinnonSuoritus =
    SureIBTutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.map(SureIBOppiaineenSuoritus.apply)),
      theoryOfKnowledgeSuoritus = s.theoryOfKnowledge,
      extendedEssay = s.extendedEssay,
      creativityActionService = s.creativityActionService,
      lisäpisteet = s.lisäpisteet,
    )
}

@Title("IB-tutkinnon oppiaineen suoritus")
case class SureIBOppiaineenSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: IBAineRyhmäOppiaine,
  predictedArviointi: Option[List[IBOppiaineenPredictedArviointi]],
) extends SureOsasuoritus

object SureIBOppiaineenSuoritus {
  def apply(s: IBOppiaineenSuoritus): SureIBOppiaineenSuoritus =
    SureIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      predictedArviointi = s.predictedArviointi,
    )
}

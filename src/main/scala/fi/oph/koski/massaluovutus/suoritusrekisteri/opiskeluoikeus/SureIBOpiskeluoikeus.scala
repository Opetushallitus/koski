package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("IB-tutkinto")
case class SureIBOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SureIBTutkinnonSuoritus],
) extends SureOpiskeluoikeus

object SureIBOpiskeluoikeus {
  def apply(oo: IBOpiskeluoikeus): Option[SureIBOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SureIBOpiskeluoikeus(
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.collect {
          case s: IBTutkinnonSuoritus => SureIBTutkinnonSuoritus(s)
        }
      )
    }
}

@Title("IB-tutkinnon suoritus")
case class SureIBTutkinnonSuoritus(
  @KoodistoKoodiarvo("ibtutkinto")
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
) extends SureSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

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
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: IBAineRyhmäOppiaine,
  predictedArviointi: Option[List[IBOppiaineenPredictedArviointi]],
) extends SureSuoritus

object SureIBOppiaineenSuoritus {
  def apply(s: IBOppiaineenSuoritus): SureIBOppiaineenSuoritus =
    SureIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      predictedArviointi = s.predictedArviointi,
    )
}

package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.massaluovutus.suorituspalvelu.SupaUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("IB-tutkinnon opiskeluoikeus")
case class SupaIBOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: LukionOpiskeluoikeudenTila,
  suoritukset: List[SupaIBTutkinnonSuoritus],
) extends SupaOpiskeluoikeus

object SupaIBOpiskeluoikeus {
  def apply(oo: IBOpiskeluoikeus, oppijaOid: String): Option[SupaIBOpiskeluoikeus] =
    when(isValmistunut(oo)) {
      SupaIBOpiskeluoikeus(
        oppijaOid = oppijaOid,
        tyyppi = oo.tyyppi,
        oid = oo.oid.get,
        koulutustoimija = oo.koulutustoimija,
        oppilaitos = oo.oppilaitos,
        tila = oo.tila,
        suoritukset = oo.suoritukset.collect {
          case s: IBTutkinnonSuoritus => SupaIBTutkinnonSuoritus(s)
        }
      )
    }
}

@Title("IB-tutkinnon suoritus")
case class SupaIBTutkinnonSuoritus(
  @KoodistoKoodiarvo("ibtutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: IBTutkinto,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[SupaIBTutkinnonOppiaine]],
  theoryOfKnowledgeSuoritus: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  lisäpisteet: Option[Koodistokoodiviite],
) extends SupaSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaIBTutkinnonSuoritus {
  def apply(s: IBTutkinnonSuoritus): SupaIBTutkinnonSuoritus =
    SupaIBTutkinnonSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.map(_.flatMap {
        case oppiaine: IBOppiaineenSuoritus => Some(SupaIBOppiaineenSuoritus(oppiaine))
        case oppiaine: IBDBCoreSuoritus => Some(SupaIBDBCoreSuoritus(oppiaine))
        case _ => None
      }),
      theoryOfKnowledgeSuoritus = s.theoryOfKnowledge,
      extendedEssay = s.extendedEssay,
      creativityActionService = s.creativityActionService,
      lisäpisteet = s.lisäpisteet,
    )
}

trait SupaIBTutkinnonOppiaine extends SupaSuoritus

@Title("IB-tutkinnon oppiaineen suoritus")
case class SupaIBOppiaineenSuoritus(
  @KoodistoKoodiarvo("iboppiaine")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: IBAineRyhmäOppiaine,
  predictedArviointi: Option[List[IBOppiaineenPredictedArviointi]],
) extends SupaIBTutkinnonOppiaine

object SupaIBOppiaineenSuoritus {
  def apply(s: IBOppiaineenSuoritus): SupaIBOppiaineenSuoritus =
    SupaIBOppiaineenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      predictedArviointi = s.predictedArviointi,
    )
}

@Title("IB-tutkinnon DP Core -oppiaineen suoritus")
case class SupaIBDBCoreSuoritus(
  @KoodistoKoodiarvo("ibcore")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: IBDPCoreOppiaine,
  arviointi: Option[List[IBOppiaineenArviointi]] = None,
) extends SupaIBTutkinnonOppiaine

object SupaIBDBCoreSuoritus {
  def apply(s: IBDBCoreSuoritus): SupaIBDBCoreSuoritus =
    SupaIBDBCoreSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi
    )
}

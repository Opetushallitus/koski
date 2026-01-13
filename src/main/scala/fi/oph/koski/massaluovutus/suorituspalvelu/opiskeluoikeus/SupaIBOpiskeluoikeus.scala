package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.massaluovutus.suorituspalvelu.SupaUtils.isValmistunut
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("IB-tutkinnon opiskeluoikeus")
case class SupaIBOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: LukionOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaIBTutkinnonSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
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
        alkamispäivä = oo.alkamispäivä,
        päättymispäivä = oo.päättymispäivä,
        suoritukset = oo.suoritukset.collect {
          case s: IBTutkinnonSuoritus => SupaIBTutkinnonSuoritus(s)
        },
        versionumero = oo.versionumero,
        aikaleima = oo.aikaleima
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
        case oppiaine: IBDPCoreSuoritus => Some(SupaIBDPCoreSuoritus(oppiaine))
        case _ => None
      }).filter(_.nonEmpty),
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
case class SupaIBDPCoreSuoritus(
  @KoodistoKoodiarvo("ibcore")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: IBDPCoreOppiaine,
  arviointi: Option[List[IBCoreOppiaineenArviointi]] = None,
) extends SupaIBTutkinnonOppiaine

object SupaIBDPCoreSuoritus {
  def apply(s: IBDPCoreSuoritus): SupaIBDPCoreSuoritus =
    SupaIBDPCoreSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      arviointi = s.arviointi
    )
}

import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBExtendedEssaySuoritus } from './IBExtendedEssaySuoritus'
import { IBCASSuoritus } from './IBCASSuoritus'
import { IBTheoryOfKnowledgeSuoritus } from './IBTheoryOfKnowledgeSuoritus'
import { IBTutkinto } from './IBTutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { IBOppiaineenSuoritus } from './IBOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.IBTutkinnonSuoritus`
 */
export type IBTutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.IBTutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  extendedEssay?: IBExtendedEssaySuoritus
  creativityActionService?: IBCASSuoritus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  lisäpisteet?: Koodistokoodiviite<'arviointiasteikkolisapisteetib', string>
  theoryOfKnowledge?: IBTheoryOfKnowledgeSuoritus
  koulutusmoduuli: IBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const IBTutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  extendedEssay?: IBExtendedEssaySuoritus
  creativityActionService?: IBCASSuoritus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  lisäpisteet?: Koodistokoodiviite<'arviointiasteikkolisapisteetib', string>
  theoryOfKnowledge?: IBTheoryOfKnowledgeSuoritus
  koulutusmoduuli?: IBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): IBTutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibtutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: IBTutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301102',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.IBTutkinnonSuoritus',
  ...o
})

export const isIBTutkinnonSuoritus = (a: any): a is IBTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBTutkinnonSuoritus'

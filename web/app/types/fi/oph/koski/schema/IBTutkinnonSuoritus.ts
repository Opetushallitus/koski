import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBExtendedEssaySuoritus } from './IBExtendedEssaySuoritus'
import { IBCASSuoritus } from './IBCASSuoritus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { IBTutkinnonOppiaineenSuoritus } from './IBTutkinnonOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { IBTheoryOfKnowledgeSuoritus } from './IBTheoryOfKnowledgeSuoritus'
import { IBTutkinto } from './IBTutkinto'

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
  ryhmä?: string
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBTutkinnonOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  lisäpisteet?: Koodistokoodiviite<'arviointiasteikkolisapisteetib', string>
  theoryOfKnowledge?: IBTheoryOfKnowledgeSuoritus
  koulutusmoduuli: IBTutkinto
}

export const IBTutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  extendedEssay?: IBExtendedEssaySuoritus
  creativityActionService?: IBCASSuoritus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBTutkinnonOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  lisäpisteet?: Koodistokoodiviite<'arviointiasteikkolisapisteetib', string>
  theoryOfKnowledge?: IBTheoryOfKnowledgeSuoritus
  koulutusmoduuli?: IBTutkinto
}): IBTutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibtutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBTutkinnonSuoritus',
  koulutusmoduuli: IBTutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301102',
      koodistoUri: 'koulutus'
    })
  }),
  ...o
})

IBTutkinnonSuoritus.className =
  'fi.oph.koski.schema.IBTutkinnonSuoritus' as const

export const isIBTutkinnonSuoritus = (a: any): a is IBTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBTutkinnonSuoritus'

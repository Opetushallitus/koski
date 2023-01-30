import { IBCoreRequirementsArviointi } from './IBCoreRequirementsArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBOppiaineTheoryOfKnowledge } from './IBOppiaineTheoryOfKnowledge'
import { IBKurssinSuoritus } from './IBKurssinSuoritus'

/**
 * Theory of Knowledge-suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.IBTheoryOfKnowledgeSuoritus`
 */
export type IBTheoryOfKnowledgeSuoritus = {
  $class: 'fi.oph.koski.schema.IBTheoryOfKnowledgeSuoritus'
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainetok'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export const IBTheoryOfKnowledgeSuoritus = (o: {
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainetok'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge
  osasuoritukset?: Array<IBKurssinSuoritus>
}): IBTheoryOfKnowledgeSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'iboppiainetok',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBTheoryOfKnowledgeSuoritus',
  ...o
})

IBTheoryOfKnowledgeSuoritus.className =
  'fi.oph.koski.schema.IBTheoryOfKnowledgeSuoritus' as const

export const isIBTheoryOfKnowledgeSuoritus = (
  a: any
): a is IBTheoryOfKnowledgeSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBTheoryOfKnowledgeSuoritus'

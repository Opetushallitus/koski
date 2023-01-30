import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBOppiaineTheoryOfKnowledge`
 */
export type IBOppiaineTheoryOfKnowledge = {
  $class: 'fi.oph.koski.schema.IBOppiaineTheoryOfKnowledge'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  pakollinen: boolean
}

export const IBOppiaineTheoryOfKnowledge = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  pakollinen: boolean
}): IBOppiaineTheoryOfKnowledge => ({
  $class: 'fi.oph.koski.schema.IBOppiaineTheoryOfKnowledge',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'TOK',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBOppiaineTheoryOfKnowledge.className =
  'fi.oph.koski.schema.IBOppiaineTheoryOfKnowledge' as const

export const isIBOppiaineTheoryOfKnowledge = (
  a: any
): a is IBOppiaineTheoryOfKnowledge =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineTheoryOfKnowledge'

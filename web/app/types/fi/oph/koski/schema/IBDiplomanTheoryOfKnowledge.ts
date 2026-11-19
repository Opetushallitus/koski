import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDiplomanTheoryOfKnowledge`
 */
export type IBDiplomanTheoryOfKnowledge = {
  $class: 'fi.oph.koski.schema.IBDiplomanTheoryOfKnowledge'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK'>
}

export const IBDiplomanTheoryOfKnowledge = (
  o: {
    tunniste?: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  } = {}
): IBDiplomanTheoryOfKnowledge => ({
  $class: 'fi.oph.koski.schema.IBDiplomanTheoryOfKnowledge',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'TOK',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBDiplomanTheoryOfKnowledge.className =
  'fi.oph.koski.schema.IBDiplomanTheoryOfKnowledge' as const

export const isIBDiplomanTheoryOfKnowledge = (
  a: any
): a is IBDiplomanTheoryOfKnowledge =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanTheoryOfKnowledge'

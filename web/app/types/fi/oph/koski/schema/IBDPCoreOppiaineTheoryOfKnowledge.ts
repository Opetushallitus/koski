import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaineTheoryOfKnowledge`
 */
export type IBDPCoreOppiaineTheoryOfKnowledge = {
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineTheoryOfKnowledge'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  laajuus?: LaajuusOpintopisteissä
  pakollinen: boolean
}

export const IBDPCoreOppiaineTheoryOfKnowledge = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  laajuus?: LaajuusOpintopisteissä
  pakollinen: boolean
}): IBDPCoreOppiaineTheoryOfKnowledge => ({
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineTheoryOfKnowledge',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'TOK',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBDPCoreOppiaineTheoryOfKnowledge.className =
  'fi.oph.koski.schema.IBDPCoreOppiaineTheoryOfKnowledge' as const

export const isIBDPCoreOppiaineTheoryOfKnowledge = (
  a: any
): a is IBDPCoreOppiaineTheoryOfKnowledge =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreOppiaineTheoryOfKnowledge'

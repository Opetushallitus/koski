import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDiplomanCoreOppiaine`
 */
export type IBDiplomanCoreOppiaine = {
  $class: 'fi.oph.koski.schema.IBDiplomanCoreOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE' | 'TOK'>
  aine?: Koodistokoodiviite<'oppiaineetib', string>
  iboOppiainekoodi?: string
}

export const IBDiplomanCoreOppiaine = (o: {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE' | 'TOK'>
  aine?: Koodistokoodiviite<'oppiaineetib', string>
  iboOppiainekoodi?: string
}): IBDiplomanCoreOppiaine => ({
  $class: 'fi.oph.koski.schema.IBDiplomanCoreOppiaine',
  ...o
})

IBDiplomanCoreOppiaine.className =
  'fi.oph.koski.schema.IBDiplomanCoreOppiaine' as const

export const isIBDiplomanCoreOppiaine = (a: any): a is IBDiplomanCoreOppiaine =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanCoreOppiaine'

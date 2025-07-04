import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaineCAS`
 */
export type IBDPCoreOppiaineCAS = {
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineCAS'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusOpintopisteissä
  pakollinen: boolean
}

export const IBDPCoreOppiaineCAS = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusOpintopisteissä
  pakollinen: boolean
}): IBDPCoreOppiaineCAS => ({
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineCAS',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'CAS',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

IBDPCoreOppiaineCAS.className =
  'fi.oph.koski.schema.IBDPCoreOppiaineCAS' as const

export const isIBDPCoreOppiaineCAS = (a: any): a is IBDPCoreOppiaineCAS =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreOppiaineCAS'

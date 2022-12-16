import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusTunneissa } from './LaajuusTunneissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBOppiaineCAS`
 */
export type IBOppiaineCAS = {
  $class: 'fi.oph.koski.schema.IBOppiaineCAS'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusTunneissa
  pakollinen: boolean
}

export const IBOppiaineCAS = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusTunneissa
  pakollinen: boolean
}): IBOppiaineCAS => ({
  $class: 'fi.oph.koski.schema.IBOppiaineCAS',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'CAS',
    koodistoUri: 'oppiaineetib'
  }),
  ...o
})

export const isIBOppiaineCAS = (a: any): a is IBOppiaineCAS =>
  a?.$class === 'IBOppiaineCAS'

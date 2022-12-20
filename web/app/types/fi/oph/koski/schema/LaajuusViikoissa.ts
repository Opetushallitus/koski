import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusViikoissa`
 */
export type LaajuusViikoissa = {
  $class: 'fi.oph.koski.schema.LaajuusViikoissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '8'>
}

export const LaajuusViikoissa = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '8'>
}): LaajuusViikoissa => ({
  $class: 'fi.oph.koski.schema.LaajuusViikoissa',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '8',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusViikoissa = (a: any): a is LaajuusViikoissa =>
  a?.$class === 'fi.oph.koski.schema.LaajuusViikoissa'

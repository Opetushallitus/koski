import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusOpintoviikoissa`
 */
export type LaajuusOpintoviikoissa = {
  $class: 'fi.oph.koski.schema.LaajuusOpintoviikoissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '1'>
}

export const LaajuusOpintoviikoissa = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '1'>
}): LaajuusOpintoviikoissa => ({
  $class: 'fi.oph.koski.schema.LaajuusOpintoviikoissa',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '1',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusOpintoviikoissa = (a: any): a is LaajuusOpintoviikoissa =>
  a?.$class === 'fi.oph.koski.schema.LaajuusOpintoviikoissa'

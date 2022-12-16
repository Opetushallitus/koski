import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusVuosiviikkotunneissa`
 */
export type LaajuusVuosiviikkotunneissa = {
  $class: 'fi.oph.koski.schema.LaajuusVuosiviikkotunneissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '3'>
}

export const LaajuusVuosiviikkotunneissa = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '3'>
}): LaajuusVuosiviikkotunneissa => ({
  $class: 'fi.oph.koski.schema.LaajuusVuosiviikkotunneissa',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '3',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusVuosiviikkotunneissa = (
  a: any
): a is LaajuusVuosiviikkotunneissa =>
  a?.$class === 'LaajuusVuosiviikkotunneissa'

import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusKursseissa`
 */
export type LaajuusKursseissa = {
  $class: 'fi.oph.koski.schema.LaajuusKursseissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '4'>
}

export const LaajuusKursseissa = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '4'>
}): LaajuusKursseissa => ({
  $class: 'fi.oph.koski.schema.LaajuusKursseissa',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '4',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

LaajuusKursseissa.className = 'fi.oph.koski.schema.LaajuusKursseissa' as const

export const isLaajuusKursseissa = (a: any): a is LaajuusKursseissa =>
  a?.$class === 'fi.oph.koski.schema.LaajuusKursseissa'

import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusOsaamispisteissä`
 */
export type LaajuusOsaamispisteissä = {
  $class: 'fi.oph.koski.schema.LaajuusOsaamispisteissä'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '6'>
}

export const LaajuusOsaamispisteissä = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '6'>
}): LaajuusOsaamispisteissä => ({
  $class: 'fi.oph.koski.schema.LaajuusOsaamispisteissä',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '6',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusOsaamispisteissä = (
  a: any
): a is LaajuusOsaamispisteissä => a?.$class === 'LaajuusOsaamispisteissä'
